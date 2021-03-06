/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.resume;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

public class SessionManager {
  static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

  private volatile boolean isDisposed;
  private final Map<String, ServerRSocketSession> sessions = new ConcurrentHashMap<>();

  public ServerRSocketSession save(ServerRSocketSession session, ByteBuf resumeToken) {
    if (isDisposed) {
      session.dispose();
    } else {
      final String token = resumeToken.toString(CharsetUtil.UTF_8);
      session
          .resumableConnection
          .onClose()
          .doFinally(
              __ -> {
                logger.debug(
                    "ResumableConnection has been closed. Removing associated session {"
                        + token
                        + "}");
                if (isDisposed || sessions.get(token) == session) {
                  sessions.remove(token);
                }
              })
          .subscribe();
      ServerRSocketSession prevSession = sessions.remove(token);
      if (prevSession != null) {
        prevSession.dispose();
      }
      sessions.put(token, session);
    }
    return session;
  }

  @Nullable
  public ServerRSocketSession get(ByteBuf resumeToken) {
    return sessions.get(resumeToken.toString(CharsetUtil.UTF_8));
  }

  public void dispose() {
    isDisposed = true;
    sessions.values().forEach(ServerRSocketSession::dispose);
  }
}
