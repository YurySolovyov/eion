'use strict';
const path = require('path');
const webpack = require('webpack');
const minificationConfig = new webpack.optimize.UglifyJsPlugin({
    output: { comments: false }
});

const ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  context: __dirname,
  entry: './src/app.js',
  output: {
    path: path.join(__dirname, '/src/compiled'),
    filename: 'app.js'
  },

  module: {
    loaders: [{
      test: /\.styl$/,
      loader: ExtractTextPlugin.extract("style-loader", "css-loader!stylus-loader")
    }, {
      test: /\.css$/,
      loader: ExtractTextPlugin.extract("style-loader", "css-loader")
    }, {
      test: /(\.jsx|app.js)$/,
      exclude: /node_modules/,
      loader: 'babel',
      query: {
        presets: ['react']
      }
    }]
  },

  resolve: {
    alias: {
      basscss: path.join(__dirname, 'node_modules/basscss/css/basscss.css')
    }
  },

  plugins: [
    // minificationConfig,
    new ExtractTextPlugin('style.css', { allChunks: true })
  ],
};
