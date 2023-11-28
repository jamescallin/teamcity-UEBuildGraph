const path = require('path')
const getWebpackConfig = require('@jetbrains/teamcity-api/getWebpackConfig')

function hackJetbrainsWebpack() {
    var config = getWebpackConfig({
        srcPath: path.join(__dirname, './src'),
        useTypeScript: true,
        entry: {
            summary: './src/summary.tsx',
            buildtab: './src/build-tab.js',
            cooktab: './src/cook-tab.js',
            assettab: './src/asset-tab.js',
        },
        output: {
            filename: '[name].bundle.js',
            path: path.resolve(__dirname, '../UEBuildGraph-server/src/main/resources/buildServerResources'),
        },
    })();

    config.output = {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, '../UEBuildGraph-server/src/main/resources/buildServerResources'),
    }

    return config;
}

module.exports = hackJetbrainsWebpack;