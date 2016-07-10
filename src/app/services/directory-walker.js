const path = require('path');
const walker = require('./promise-walker');

const walk = function(directoryPath) {
  const results = {
    files: [],
    directories: []
  };

  return walker(directoryPath).then(function(items) {
    items.forEach(function(item) {
      if (item.stat) {
        if (item.stat.isDirectory()) {
          results.directories.push({
            name: path.basename(item.path),
            stat: item.stat,
            absolute: item.path
          });
        } else if(item.stat.isFile()) {
          results.files.push({
            name: path.basename(item.path),
            stat: item.stat,
            absolute: item.path
          });
        }
      } else {
        if (path.extname(item.path) === '') {
          results.directories.push({
            name: path.basename(item.path),
            stat: null,
            absolute: item.path
          });
        } else {
          results.files.push({
            name: path.basename(item.path),
            stat: null,
            absolute: item.path
          });
        }
      }
    });

    return results;
  });
};

module.exports = function walkdir(path) {
  return walk(path);
};
