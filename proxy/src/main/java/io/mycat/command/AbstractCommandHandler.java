package io.mycat.command;

import io.mycat.proxy.ProxyRuntime;
import io.mycat.proxy.reactor.MycatReactorThread;
import io.mycat.proxy.reactor.NIOJob;
import io.mycat.proxy.reactor.ReactorEnvThread;
import io.mycat.proxy.session.MycatSession;
import io.mycat.proxy.session.SessionManager.FrontSessionManager;
import java.util.Map;

public abstract class AbstractCommandHandler implements CommandDispatcher {


  public void handleSleep(MycatSession session) {
    session.writeErrorEndPacket();
  }


  public void handleRefresh(int subCommand, MycatSession session) {
    session.writeErrorEndPacket();
  }


  public void handleShutdown(int shutdownType, MycatSession session) {
    session.writeErrorEndPacket();
  }


  public void handleConnect(MycatSession session) {
    session.writeErrorEndPacket();
  }


  public void handleDebug(MycatSession session) {
    session.writeErrorEndPacket();
  }


  public void handleTime(MycatSession session) {
    session.writeErrorEndPacket();
  }


  public void handleDelayedInsert(MycatSession session) {
    session.writeErrorEndPacket();
  }


  public void handleDaemon(MycatSession session) {
    session.writeErrorEndPacket();
  }

  @Override
  public void handleInitDb(String db, MycatSession mycat) {
    mycat.useSchema(db);
    mycat.writeOkEndPacket();
  }

  @Override
  public void handleQuit(MycatSession mycat) {
    mycat.close(true, "quit");
  }

  @Override
  public void handlePing(MycatSession mycat) {
    mycat.writeOkEndPacket();
  }

  @Override
  public void handleFieldList(String table, String filedWildcard, MycatSession mycat) {
    mycat.setLastMessage("mycat unsupport  handleFieldList");
    mycat.writeErrorEndPacket();
  }

  @Override
  public void handleSetOption(boolean on, MycatSession mycat) {
    mycat.setMultiStatementSupport(on);
    mycat.writeOkEndPacket();
    return;
  }

  @Override
  public void handleCreateDb(String schemaName, MycatSession mycat) {
    mycat.setLastMessage("mycat unsupport handleCreateDb");
    mycat.writeErrorEndPacket();
  }

  @Override
  public void handleDropDb(String schemaName, MycatSession mycat) {
    mycat.setLastMessage("mycat unsupport  handleDropDb");
    mycat.writeErrorEndPacket();
  }

  @Override
  public void handleStatistics(MycatSession mycat) {
    mycat.setLastMessage("mycat unsupport  handleStatistics");
    mycat.writeErrorEndPacket();
  }

  @Override
  public void handleProcessInfo(MycatSession mycat) {
    mycat.setLastMessage("mycat unsupport  handleProcessInfo");
    mycat.writeErrorEndPacket();
  }

  @Override
  public void handleChangeUser(String userName, String authResponse, String schemaName,
      int charsetSet, String authPlugin, Map<String, String> clientConnectAttrs,
      MycatSession mycat) {
    mycat.setLastMessage("mycat unsupport  handleChangeUser");
    mycat.writeErrorEndPacket();
  }

  @Override
  public void handleResetConnection(MycatSession mycat) {
    mycat.resetSession();
    mycat.setLastMessage("mycat unsupport  handleResetConnection");
    mycat.writeErrorEndPacket();
  }

  @Override
  public void handleProcessKill(long connectionId, MycatSession mycat) {
    ProxyRuntime runtime = mycat.getIOThread().getRuntime();
    MycatReactorThread[] mycatReactorThreads = runtime.getMycatReactorThreads();
    MycatReactorThread currentThread = mycat.getIOThread();
    for (MycatReactorThread mycatReactorThread : mycatReactorThreads) {
      FrontSessionManager<MycatSession> frontManager = mycatReactorThread.getFrontManager();
      for (MycatSession allSession : frontManager.getAllSessions()) {
        if (allSession.sessionId() == connectionId) {
          if (currentThread == mycatReactorThread) {
            allSession.close(true, "processKill");
          } else {
            mycatReactorThread.addNIOJob(new NIOJob() {
              @Override
              public void run(ReactorEnvThread reactor) throws Exception {
                allSession.close(true, "processKill");
              }

              public void stop(ReactorEnvThread reactor, Exception reason) {
                allSession.close(true, "processKill");
              }

              @Override
              public String message() {
                return "processKill";
              }
            });
          }
          mycat.writeOkEndPacket();
          return;
        }
      }
    }
    mycat.writeErrorEndPacket();
  }
}
