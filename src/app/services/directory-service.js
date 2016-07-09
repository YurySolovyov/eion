const Promise = require('bluebird');
const path = require('path');
const { shell, remote } = require('electron');
const { app } = remote;
const walkdir = require('./directory-walker');


const getInitialPath = function() {
  return path.dirname(app.getPath('exe'));
};

const getDirectoryItems = walkdir;

const openPath = function(pathToOpen) {
  shell.openExternal(pathToOpen);
};

const initialize = function(store, leftPanelPath, rightPanelPath) {
  const leftDirs = getDirectoryItems(leftPanelPath);
  const rightDirs = getDirectoryItems(rightPanelPath);
  Promise.join(leftDirs, rightDirs).then(function(results) {
    const [leftPanelItems, rightPanelItems] = results;

    store.dispatch({
      type: 'NAVIGATE',
      path: leftPanelPath,
      items: leftPanelItems,
      panel: 'left'
    });

    store.dispatch({
      type: 'NAVIGATE',
      path: rightPanelPath,
      items: rightPanelItems,
      panel: 'right'
    });
  });
};

module.exports = {
  initialize,
  getInitialPath,
  getDirectoryItems,
  openPath
};
