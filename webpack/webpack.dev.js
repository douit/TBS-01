const webpack = require('webpack');
const writeFilePlugin = require('write-file-webpack-plugin');
const webpackMerge = require('webpack-merge');
const BrowserSyncPlugin = require('browser-sync-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');
const FriendlyErrorsWebpackPlugin = require('friendly-errors-webpack-plugin');
const SimpleProgressWebpackPlugin = require('simple-progress-webpack-plugin');
const WebpackNotifierPlugin = require('webpack-notifier');
const path = require('path');
const sass = require('sass');

const utils = require('./utils.js');
const commonConfig = require('./webpack.common.js');

const ENV = 'development';

module.exports = (options) => webpackMerge(commonConfig({ env: ENV }), {
    devtool: 'eval-source-map',
    devServer: {
        contentBase: './target/classes/static/',
        proxy: [{
            context: [
                '/api',
                '/billing',
                '/services',
                '/management',
                '/swagger-resources',
                '/v2/api-docs',
                '/h2-console',
                '/auth'
            ],
            target: `http${options.tls ? 's' : ''}://localhost:8081`,
            secure: false,
            changeOrigin: options.tls
        }],
        stats: options.stats,
        watchOptions: {
            ignored: /node_modules/
        },
        https: options.tls,
        historyApiFallback: true
    },
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
        filename: 'app/[name].bundle.js',
        chunkFilename: 'app/[id].chunk.js'
    },
    module: {
        rules: [{
            test: /\.ts$/,
            use: [
                'angular2-template-loader',
                {
                    loader: 'cache-loader',
                    options: {
                      cacheDirectory: path.resolve('target/cache-loader')
                    }
                },
                {
                    loader: 'thread-loader',
                    options: {
                        // There should be 1 cpu for the fork-ts-checker-webpack-plugin.
                        // The value may need to be adjusted (e.g. to 1) in some CI environments,
                        // as cpus() may report more cores than what are available to the build.
                        workers: require('os').cpus().length - 1
                    }
                },
                {
                    loader: 'ts-loader',
                    options: {
                        transpileOnly: true,
                        happyPackMode: true
                    }
                }
            ],
            exclude: /(node_modules)/
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
            use: ['style-loader', 'css-loader', 'postcss-loader', {
                loader: 'sass-loader',
                options: { implementation: sass }
            }]
        },
        {
            test: /\.css$/,
            use: ['to-string-loader', 'css-loader'],
            exclude: /(vendor\.css|global\.css)/
        }]
    },
    stats: process.env.JHI_DISABLE_WEBPACK_LOGS ? 'none' : options.stats,
    plugins: [
        process.env.JHI_DISABLE_WEBPACK_LOGS
            ? null
            : new SimpleProgressWebpackPlugin({
                format: options.stats === 'minimal' ? 'compact' : 'expanded'
              }),
        new FriendlyErrorsWebpackPlugin(),
        new ForkTsCheckerWebpackPlugin(),
        new BrowserSyncPlugin({
            https: options.tls,
            host: 'localhost',
            port: 9000,
            proxy: {
                target: `http${options.tls ? 's' : ''}://localhost:9060`,
                proxyOptions: {
                    changeOrigin: false  //pass the Host header to the backend unchanged  https://github.com/Browsersync/browser-sync/issues/430
                }
            },
            socket: {
                clients: {
                    heartbeatTimeout: 60000
                }
            }
        }, {
            reload: false
        }),
        new webpack.ContextReplacementPlugin(
            /angular(\\|\/)core(\\|\/)/,
            path.resolve(__dirname, './src/main/webapp/')
        ),
        new writeFilePlugin(),
        new webpack.WatchIgnorePlugin([
            utils.root('src/test'),
        ]),
        new WebpackNotifierPlugin({
            title: 'TBS',
            contentImage: path.join(__dirname, '../src/webapp/content/img/logo.png')
        })
    ].filter(Boolean),
    mode: 'development'
});
