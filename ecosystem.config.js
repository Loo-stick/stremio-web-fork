module.exports = {
  apps: [{
    name: 'stremio-fork',
    script: 'http_server.js',
    cwd: '/projets/stremio-web-fork',
    env: {
      NODE_ENV: 'production'
    },
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '500M'
  }]
};
