const path = __non_webpack_require__('path');
const fs = __non_webpack_require__('fs');
const { shell, remote } = __non_webpack_require__('electron');
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
