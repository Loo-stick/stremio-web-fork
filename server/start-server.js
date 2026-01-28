#!/usr/bin/env node
/**
 * Wrapper pour le streaming server Stremio
 */

const path = require('path');

// Configuration
process.env.APP_PATH = __dirname;
process.env.NO_CORS = '1';

// Lancer le server
require('./server.js');
