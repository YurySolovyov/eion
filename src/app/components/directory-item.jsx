const React = require('react');
const FontAwesome = require('react-fontawesome');

module.exports = React.createClass({

  handleDoubleClick: function() {
    const { type, name } = this.props;
    if (type === 'file') {
      this.props.open(name);
    } else {
      this.props.navigate(name);
    }
  },

  getIcon(props) {
    if (props.type === 'file') {
      const icon = props.stat ? 'file-o' : 'lock';
      return <FontAwesome name={icon} className='px1' />
    } else {
      const icon = props.stat ? 'folder-o' : 'lock';
      return <FontAwesome name={icon} className='px1' />
    }
  },

  render: function() {
    return (
      <div className="directory-item p1" onDoubleClick={this.handleDoubleClick} >
        {this.getIcon(this.props)}
        <span>{this.props.name}</span>
      </div>
    );
  }
});
