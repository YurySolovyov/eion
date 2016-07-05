const React = require('react');
const Panel = require('./panel');

const DirectoryService = require('../services/directory-service');

module.exports = React.createClass({
  render: function() {
    const items = DirectoryService.getDirectoryItems();
    return (
      <div id="container">
        <div id="panelsContainer" className="flex">
          <Panel
            id="leftPanel"
            items={items} />
          <Panel
            id="rightPanel"
            items={items} />
        </div>
      </div>
    );
  }
});
