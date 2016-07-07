const path = require('path');
const fs = require('fs');
const { shell, remote } = require('electron');
const app = remote.app;


const getInitialPath = function() {
  return path.dirname(app.getPath('exe'));
};

const getDirectoryItems = function(directoryPath) {
  return fs.readdirSync(directoryPath);
};

const openPath = function(pathToOpen) {
  shell.openExternal(pathToOpen);
};

module.exports = {
  getInitialPath,
  getDirectoryItems,
  openPath
};
