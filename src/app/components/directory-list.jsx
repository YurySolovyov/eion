const React = require('react');
const DirectoryItem = require('./directory-item');

module.exports = React.createClass({
  upDirectoryProps: {
    name: '..',
    stat: {
      isDirectory: () => { return true; }
    }
  },

  renderItems: function(items, type) {
    const navigate = this.props.navigate;
    return items.map((item, index) => {
      return this.renderItem(index, item, type);
    });
  },

  renderItem: function(key, item, type) {
    return (
      <DirectoryItem
        {...item}
        key={key}
        type={type}
        navigate={this.props.navigate}
        open={this.props.open} />
    );
  },

  render: function() {
    return (
      <div className="directory-list mx2">
        {this.renderItem('up', this.upDirectoryProps, 'dir')}
        {this.renderItems(this.props.items.directories, 'dir')}
        {this.renderItems(this.props.items.files, 'file')}
      </div>
    );
  }
});
