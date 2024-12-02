import { ExecutionResult } from 'graphql';

import { EventEmitter } from 'events';
import {

  Client,
} from './client.js';
import {
  SubscribePayload,
} from './common.js';

export interface TSubscribe<T> {
  waitForNext: (
    test?: (value: ExecutionResult<T, unknown>) => void,
    expire?: number,
  ) => Promise<void>;
  waitForError: (
    test?: (error: unknown) => void,
    expire?: number,
  ) => Promise<void>;
  waitForComplete: (test?: () => void, expire?: number) => Promise<void>;
  dispose: () => void;
}

export function tsubscribe<T = unknown>(
  client: Client,
  payload: SubscribePayload,
): TSubscribe<T> {
  const emitter = new EventEmitter();
  const results: ExecutionResult<T, unknown>[] = [];
  let error: unknown,
    completed = false;
  const dispose = client.subscribe<T>(payload, {
    next: (value: any) => {
      results.push(value);
      emitter.emit('next');
    },
    error: (err: any) => {
      error = err;
      emitter.emit('err');
      emitter.removeAllListeners();
    },
    complete: () => {
      completed = true;
      emitter.emit('complete');
      emitter.removeAllListeners();
    },
  });

  return {
    waitForNext: (test, expire) => {
      return new Promise((resolve) => {
        function done() {
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          const result = results.shift()!;
          test?.(result);
          resolve();
        }
        if (results.length > 0) return done();
        emitter.once('next', done);
        if (expire)
          setTimeout(() => {
            emitter.off('next', done); // expired
            resolve();
          }, expire);
      });
    },
    waitForError: (test, expire) => {
      return new Promise((resolve) => {
        function done() {
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          test?.(error);
          resolve();
        }
        if (error) return done();
        emitter.once('err', done);
        if (expire)
          setTimeout(() => {
            emitter.off('err', done); // expired
            resolve();
          }, expire);
      });
    },
    waitForComplete: (test, expire) => {
      return new Promise((resolve) => {
        function done() {
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          test?.();
          resolve();
        }
        if (completed) return done();
        emitter.once('complete', done);
        if (expire)
          setTimeout(() => {
            emitter.off('complete', done); // expired
            resolve();
          }, expire);
      });
    },
    dispose,
  };
}

export const authenticate = async (): Promise<any> => {

  const url = 'http://localhost:8080/realms/master/protocol/openid-connect/token';

  const body = new URLSearchParams();
  body.append('grant_type', 'password');
  body.append('client_id', 'keycloak-websocket');
  body.append('client_secret', 'bKggQcsaUsC2WkS5rhrhgdDB91sjgkXf');
  body.append('username', 'admin');
  body.append('password', 'admin');

  const response = await fetch(
    url,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: body.toString()
    });

  return response.json();
}