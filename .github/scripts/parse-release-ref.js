#!/usr/bin/env node

const fs = require("fs");

function fail(message) {
    console.error(`::error::${message}`);
    process.exit(1);
}

const ref = process.argv[2] || process.env.GITHUB_REF_NAME;

if (!ref) {
    fail("Release ref is empty. Pass tag as argument or set GITHUB_REF_NAME.");
}

const match = /^mc(?<mc>[0-9]+(?:\.[0-9]+)*)-v(?<mod>[0-9A-Za-z][0-9A-Za-z.+-]*)$/.exec(ref);

if (!match || !match.groups) {
    fail(
        `Invalid release tag "${ref}"`
    );
}

const minecraftLine = match.groups.mc;
const modVersion = match.groups.mod;

const result = {
    releaseTag: ref,
    minecraftLine,
    modVersion,
};

console.log(JSON.stringify(result, null, 2));

const outputPath = process.env.GITHUB_OUTPUT;
if (outputPath) {
    fs.appendFileSync(outputPath, `release_tag=${ref}\n`);
    fs.appendFileSync(outputPath, `minecraft_line=${minecraftLine}\n`);
    fs.appendFileSync(outputPath, `mod_version=${modVersion}\n`);
}