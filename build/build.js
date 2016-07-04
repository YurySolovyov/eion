'use strict';
console.time('Build');

const path = require('path');
const packager = require('electron-packager');
const copyFileSync = require('./copy-file-sync');

copyFileSync(path.resolve('./package.json'), './src/compiled/package.json');
copyFileSync(path.resolve('./src/index.js'), './src/compiled/index.js');
copyFileSync(path.resolve('./src/index.html'), './src/compiled/index.html');

const pack = require('../package.json');
const options = {
  dir: './src/compiled',
  platform: ['win32', 'linux'],
  arch: ['x64'],
  version: '1.2.5',

  prune: true,
  overwrite: true,
  out: path.join('./out', pack.version)
};

packager(options, function (err, appPath) {
  console.timeEnd('Build');
  console.log(err, appPath);
});
