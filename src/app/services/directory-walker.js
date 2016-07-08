const Promise = require('bluebird');
const filewalker = require('filewalker');
const walkerOptions = { recursive: false, maxPending: 7 };

const getWalker = Promise.promisify(function(path, results, callback) {
  return filewalker(path, walkerOptions).on('file', function(name, stats, absolute) {
    results.files.push({
      name,
      stats,
      absolute
    });
  }).on('dir', function(name, stats, absolute) {
    results.directories.push({
      name,
      stats,
      absolute
    });
  }).on('done', function() {
    callback(null, results);
  }).on('error', function(err) {
    callback(err);
  }).walk();
});

module.exports = function walkdir(path) {
  return getWalker(path, {
    files: [],
    directories: []
  });
};
