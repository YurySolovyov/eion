const React = require('react');
const DirectoryItem = require('./directory-item');

module.exports = React.createClass({

  renderItems: function(items) {
    return items.map(function(item, index) {
      return (
        <DirectoryItem key={index} label={item} />
      );
    });
  },

  render: function() {
    return (
      <div className="panel flex" id={this.props.id}>
        {this.renderItems(this.props.items)}
      </div>
    );
  }
});
