
import express from 'express';
import http from 'http';
import cors from 'cors';

import { createProxyMiddleware } from 'http-proxy-middleware';

import session, {MemoryStore} from "express-session";
const memoryStore = new MemoryStore();

const app = express();
const httpServer = http.createServer(app);

app.use(session({
  secret: 'some secret',
  resave: false,
  saveUninitialized: true,
  store: memoryStore
}));


app.use(
  cors()
);

const simpleRequestLogger = (proxyServer, options) => {
    proxyServer.on('proxyReq', (proxyReq, req, res) => {
      console.log(`[HPM] [${req.method}] ${req.url} ${JSON.stringify(req.headers)}`); // outputs: [HPM] GET /users
    });
  };

app.use(
  '/graphql',
  createProxyMiddleware({
    target: 'http://localhost:8080/realms/master/graphql',
    changeOrigin: true,
    plugins:[simpleRequestLogger]
  })
);

app.use(express.static('public'));

await new Promise((resolve) => httpServer.listen({ port: 4000 }, resolve));
console.log(`🚀 Server ready at http://localhost:4000`);