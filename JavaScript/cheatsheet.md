- JavaScriptチートシート
https://jsprimer.net/cheatsheet/


# Node.jsからWebAPIを叩く基本のコード

GETまたはPOSTでデータを渡し、JSONで返ってくる場合を想定する。


```javascript:
const http = require('http');
const express = require('express');
const async = require('async');
const app = express();


const APIKEY   = "APIkey";
const URL      = "APIのURL";
```
