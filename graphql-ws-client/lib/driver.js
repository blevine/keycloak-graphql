"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const client_1 = require("./client");
const client = (0, client_1.createClient)({
    url: 'ws://localhost:8081/graphql',
    lazy: false,
    on: {
        connected: () => {
            console.log("Connected!!!");
        },
        closed: (event) => {
            console.log(`Closed: reason = ${event.reason}, code = ${event.code}`);
            //done();
        },
        error: (error) => {
            console.error('Error: ', error);
        },
        ping: (received) => {
            console.log(`Ping: received(${received})`);
        },
        pong: (received) => {
            console.log(`Pong: received(${received}`);
        },
        message: (message) => {
            console.log('Message: ', message);
        }
    }
});
