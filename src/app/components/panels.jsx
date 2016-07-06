const React = require('react');
const Panel = require('./panel');

module.exports = React.createClass({
  render: function() {
    console.log(this.props);
    return (
      <div id="panelsContainer" className="flex">
        <Panel
          {...this.props}
          id="leftPanel"
          items={this.props.leftPanel.directoryItems}
        />
        <Panel
          {...this.props}
          id="rightPanel"
          items={this.props.rightPanel.directoryItems}
        />
      </div>
    );
  }
});
