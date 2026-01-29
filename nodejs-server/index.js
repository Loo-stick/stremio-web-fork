/**
 * Stremio Streaming Server Entry Point for Android
 * Configures ffmpeg paths and starts the server
 */

const os = require('os');
const path = require('path');
const { channel } = require('bridge');

// Detect CPU architecture
const arch = os.arch();
console.log('[StreamingServer] CPU Architecture:', arch);

// Determine ffmpeg folder based on architecture
let ffmpegFolder;
if (arch === 'arm64' || arch === 'aarch64') {
    ffmpegFolder = 'arm64';
} else if (arch === 'arm' || arch.startsWith('arm')) {
    ffmpegFolder = 'arm';
} else {
    console.warn('[StreamingServer] Unknown architecture:', arch, '- defaulting to arm64');
    ffmpegFolder = 'arm64';
}

// Set ffmpeg paths
const ffmpegDir = path.join(__dirname, 'ffmpeg', ffmpegFolder);
const ffmpegPath = path.join(ffmpegDir, 'ffmpeg');
const ffprobePath = path.join(ffmpegDir, 'ffprobe');

console.log('[StreamingServer] FFmpeg path:', ffmpegPath);
console.log('[StreamingServer] FFprobe path:', ffprobePath);

// Set environment variables for server.js
process.env.FFMPEG_BIN = ffmpegPath;
process.env.FFPROBE_BIN = ffprobePath;
process.env.NO_CORS = '1'; // Disable CORS checks for local use

// Notify Capacitor that we're starting
channel.send('server-status', { status: 'starting' });

// Start the server
console.log('[StreamingServer] Starting Stremio streaming server...');

try {
    require('./server.js');

    // Server started successfully
    const serverUrl = 'http://127.0.0.1:11470';
    console.log('[StreamingServer] Server started at:', serverUrl);

    channel.send('server-status', {
        status: 'running',
        url: serverUrl,
        port: 11470
    });
} catch (error) {
    console.error('[StreamingServer] Failed to start server:', error);
    channel.send('server-status', {
        status: 'error',
        error: error.message
    });
}

// Listen for messages from Capacitor
channel.addListener('server-command', (message) => {
    console.log('[StreamingServer] Received command:', message);

    if (message === 'status') {
        channel.send('server-status', {
            status: 'running',
            url: 'http://127.0.0.1:11470',
            port: 11470
        });
    }
});

console.log('[StreamingServer] Entry point initialized');
