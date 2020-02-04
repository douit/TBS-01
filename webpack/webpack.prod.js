const webpack = require('webpack');
const webpackMerge = require('webpack-merge');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const Visualizer = require('webpack-visualizer-plugin');
const MomentLocalesPlugin = require('moment-locales-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const WorkboxPlugin = require('workbox-webpack-plugin');
const AngularCompilerPlugin = require('@ngtools/webpack').AngularCompilerPlugin;
const path = require('path');

const utils = require('./utils.js');
const commonConfig = require('./webpack.common.js');

const ENV = 'production';
const sass = require('sass');

module.exports = webpackMerge(commonConfig({ env: ENV }), {
    // Enable source maps. Please note that this will slow down the build.
    // You have to enable it in Terser config below and in tsconfig-aot.json as well
    // devtool: 'source-map',
    entry: {
        polyfills: './src/main/webapp/polyfills.ts',
        global: './src/main/webapp/content/scss/global.scss',
        main: './src/main/webapp/main.ts',
      styles: [
        "./node_modules/perfect-scrollbar/css/perfect-scrollbar.css",
        "./node_modules/angular-calendar/scss/angular-calendar.scss",
        "./node_modules/sweetalert2/src/sweetalert2.scss",
        "./src/main/webapp/content/scss/global.scss",
        "./node_modules/datatables.net-bs4/css/dataTables.bootstrap4.css",
        "./src/main/webapp/content/scss/material-dashboard.scss",
        "./node_modules/bootstrap-rtl/dist/css/bootstrap-rtl.css"
      ],
      scripts: [
        "./node_modules/jquery/dist/jquery.js",
        "./node_modules/popper.js/dist/umd/popper.js",
        "./node_modules/bootstrap-material-design/dist/js/bootstrap-material-design.min.js",
        "./node_modules/moment/moment.js",
        "./node_modules/arrive/src/arrive.js",
        "./node_modules/hammerjs/hammer.min.js",
        "./node_modules/web-animations-js/web-animations.min.js",
        "./node_modules/chartist/dist/chartist.js",
        "./node_modules/chartist-plugin-zoom/dist/chartist-plugin-zoom.js",
        "./node_modules/twitter-bootstrap-wizard/jquery.bootstrap.wizard.js",
        "./node_modules/bootstrap-notify/bootstrap-notify.js",
        "./node_modules/nouislider/distribute/nouislider.min.js",
        "./node_modules/bootstrap-select/dist/js/bootstrap-select.js",
        "./node_modules/datatables/media/js/jquery.dataTables.js",
        "./node_modules/datatables.net-bs4/js/dataTables.bootstrap4.js",
        "./node_modules/datatables.net-responsive/js/dataTables.responsive.js",
        "./node_modules/fullcalendar/dist/fullcalendar.js",
        "./node_modules/bootstrap-tagsinput/dist/bootstrap-tagsinput.js",
        "./node_modules/jasny-bootstrap/dist/js/jasny-bootstrap.min.js",
        "./node_modules/perfect-scrollbar/dist/perfect-scrollbar.min.js",
        "./node_modules/jqvmap/dist/jquery.vmap.min.js",
        "./node_modules/jqvmap/dist/maps/jquery.vmap.world.js",
        "./node_modules/jquery-validation/dist/jquery.validate.min.js"
      ]
    },
    output: {
        path: utils.root('target/classes/static/'),
        filename: 'app/[name].[hash].bundle.js',
        chunkFilename: 'app/[id].[hash].chunk.js'
    },
    module: {
        rules: [{
            test: /(?:\.ngfactory\.js|\.ngstyle\.js|\.ts)$/,
            loader: '@ngtools/webpack'
        },
        {
            test: /\.scss$/,
            use: ['to-string-loader', 'css-loader', {
                loader: 'sass-loader',
                options: { implementation: sass }
            }],
            exclude: /(vendor\.scss|global\.scss)/
        },
        {
            test: /(vendor\.scss|global\.scss)/,
            use: [
                {
                    loader: MiniCssExtractPlugin.loader,
                    options: {
                        publicPath: '../'
                    }
                },
                'css-loader',
                'postcss-loader',
                {
                    loader: 'sass-loader',
                    options: { implementation: sass }
                }
            ]
        },
        {
            test: /\.css$/,
            use: ['to-string-loader', 'css-loader'],
            exclude: /(vendor\.css|global\.css)/
        },
        {
            test: /(vendor\.css|global\.css)/,
            use: [
                {
                    loader: MiniCssExtractPlugin.loader,
                    options: {
                        publicPath: '../'
                    }
                },
                'css-loader',
                'postcss-loader'
            ]
        }]
    },
    optimization: {
        runtimeChunk: false,
        minimizer: [
            new TerserPlugin({
                parallel: true,
                cache: true,
                // sourceMap: true, // Enable source maps. Please note that this will slow down the build
                terserOptions: {
                    ecma: 6,
                    ie8: false,
                    toplevel: true,
                    module: true,
                    compress: {
                        dead_code: true,
                        warnings: false,
                        properties: true,
                        drop_debugger: true,
                        conditionals: true,
                        booleans: true,
                        loops: true,
                        unused: true,
                        toplevel: true,
                        if_return: true,
                        inline: true,
                        join_vars: true,
                        ecma: 6,
                        module: true,
                        toplevel: true
                    },
                    output: {
                        comments: false,
                        beautify: false,
                        indent_level: 2,
                        ecma: 6
                    },
                    mangle: {
                        module: true,
                        toplevel: true
                    }
                }
            }),
            new OptimizeCSSAssetsPlugin({})
        ]
    },
    plugins: [
        new MiniCssExtractPlugin({
            // Options similar to the same options in webpackOptions.output
            // both options are optional
            filename: 'content/[name].[contenthash].css',
            chunkFilename: 'content/[id].css'
        }),
        new MomentLocalesPlugin({
            localesToKeep: [
                    'ar-ly',
                    'en'
                    // jhipster-needle-i18n-language-moment-webpack - JHipster will add/remove languages in this array
                ]
        }),
        new Visualizer({
            // Webpack statistics in target folder
            filename: '../stats.html'
        }),
        new AngularCompilerPlugin({
            mainPath: utils.root('src/main/webapp/main.ts'),
            tsConfigPath: utils.root('tsconfig-aot.json'),
            sourceMap: true
        }),
        new webpack.LoaderOptionsPlugin({
            minimize: true,
            debug: false
        }),
        new WorkboxPlugin.GenerateSW({
          clientsClaim: true,
          skipWaiting: true,
        })
    ],
    mode: 'production'
});
