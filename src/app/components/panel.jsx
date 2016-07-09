const React = require('react');
const DirectoryList = require('./directory-list');
const Locations = require('./locations');

module.exports = React.createClass({

  handleClick: function() {
    this.props.activatePanel(this.props.id);
  },

  render: function() {
    const active = this.props.currentPanel === this.props.id;
    return (
      <div
        className={`panel flex ${ active ? 'active' : '' }`}
        id={this.props.id}
        onClick={this.handleClick}>
        <Locations {...this.props} />
        <DirectoryList
          {...this.props}
          navigate={this.props.navigate}
        />
      </div>
    );
  }
});
