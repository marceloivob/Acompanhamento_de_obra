// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html

process.env.CHROME_BIN = require('puppeteer').executablePath()

module.exports = function (config) {
  config.set({
    basePath: '',
    preprocessors: {
      // source files, that you wanna generate coverage for
      // do not include tests or libraries
     'src/**/!(*spec|*mock).js': ['coverage']	  
    },
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage-istanbul-reporter'),
      require('@angular-devkit/build-angular/plugins/karma'),
      require('karma-sonarqube-reporter'),
//      require('karma-webpack'), 
      require('karma-coverage')
    ],
     sonarqubeReporter: {
     basePath: 'src/app',        // test folder
     filePattern: '**/*spec.ts', // test file pattern
     outputFolder: 'reports',    // reports destination
     encoding: 'utf-8'           // file format
    },
    client: {
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    coverageIstanbulReporter: {
      dir: require('path').join(__dirname, '../coverage'),
      reports: ['html', 'lcovonly', 'text-summary'],
      fixWebpackSourcePaths: true
    },
    customLaunchers: {
      ChromeDebugging: {
        base: 'Chrome',
        flags: [ '--remote-debugging-port=9333' ]
      },
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: [
            '--headless', '--disable-dev-shm-usage',
            '--no-sandbox', // required to run without privileges in docker
            '--user-data-dir=/tmp/chrome-test-profile',
            '--disable-setuid-sandbox',
            '--disable-gpu',
            '--disable-web-security'
        ]
      }
    },
    reporters: ['progress', 'kjhtml', 'sonarqube', 'coverage'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['ChromeDebugging', 'ChromeHeadlessNoSandbox'],
    browserDisconnectTimeout: 10000,
    browserDisconnectTolerance: 3,
    browserNoActivityTimeout: 60000,
    singleRun: false
  });
};
