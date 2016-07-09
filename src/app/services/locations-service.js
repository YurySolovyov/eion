const { spawn } = require('child_process');
const { StringDecoder } = require('string_decoder');
const Promise = require('bluebird');
const splitLines = require('split-lines');

const parseDrives = function(lines) {
  return splitLines(lines.join('')).filter(function(line) {
    return line.match(/^\w:\s+$/);
  }).map(function(drive) {
    const trimmed = drive.trim();
    return {
      path: trimmed.toLowerCase(),
      name: trimmed
    };
  });
};

const getLocationsWindows = Promise.promisify(function(callback) {
  const lines = [];
  const list = spawn('cmd');
  const decoder = new StringDecoder('utf8');

  list.stdout.on('data', function (data) {
    lines.push(decoder.write(data));
  });

  list.on('exit', function (code) {
    if (code === 0) {
      callback(null, parseDrives(lines))
    } else {
      callback(new Error('Error getting drives list'));
      // error getting Locations
    }
  });

  list.stdin.write('wmic logicaldisk get name\n');
  list.stdin.end();
});

const getLocations = function() {
  return getLocationsWindows();
};

const initialize = function(store) {
  getLocations().then(function(locations) {
    store.dispatch({
      type: 'UPDATE_LOCATIONS',
      locations
    });
  });
};

module.exports = {
  initialize
};
