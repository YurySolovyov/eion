'use strict';
console.time('Build');

const path = require('path');
const packager = require('electron-packager');

const pack = require('../package.json');
const options = {
  dir: './src/compiled',
  platform: ['win32', 'linux'],
  arch: ['x64'],
  version: '1.2.6',

  prune: true,
  overwrite: true,
  out: path.join('./out', pack.version)
};

packager(options, function (err, appPath) {
  console.timeEnd('Build');
  if (err) {
    console.log(err, appPath);
  }
});
