/**
 * Service to manage the embedded streaming server on Android
 */

let serverUrl = null;
let serverStatus = 'unknown';
let nodeJSAvailable = false;

// Check if we're running in Capacitor with NodeJS support
async function initStreamingServer() {
    try {
        const { Capacitor } = await import('@capacitor/core');

        // Only initialize on Android
        if (Capacitor.getPlatform() !== 'android') {
            console.log('[StreamingServerService] Not on Android, skipping embedded server');
            return null;
        }

        const { NodeJS } = await import('capacitor-nodejs');
        nodeJSAvailable = true;

        console.log('[StreamingServerService] Initializing embedded streaming server...');

        // Listen for server status updates
        NodeJS.addListener('server-status', (event) => {
            const data = event.args[0];
            console.log('[StreamingServerService] Server status:', data);

            serverStatus = data.status;
            if (data.url) {
                serverUrl = data.url;
            }

            // Dispatch custom event for the app to react
            window.dispatchEvent(new CustomEvent('streamingServerStatus', {
                detail: data
            }));
        });

        // Wait for Node.js to be ready
        await NodeJS.whenReady();
        console.log('[StreamingServerService] Node.js runtime is ready');

        // The server should start automatically via index.js
        // Wait a bit for it to initialize
        await new Promise(resolve => setTimeout(resolve, 2000));

        // Request status
        NodeJS.send({
            eventName: 'server-command',
            args: ['status']
        });

        return serverUrl;
    } catch (error) {
        console.log('[StreamingServerService] Embedded server not available:', error.message);
        return null;
    }
}

function getServerUrl() {
    return serverUrl;
}

function getServerStatus() {
    return serverStatus;
}

function isEmbeddedServerAvailable() {
    return nodeJSAvailable && serverStatus === 'running';
}

export {
    initStreamingServer,
    getServerUrl,
    getServerStatus,
    isEmbeddedServerAvailable
};
