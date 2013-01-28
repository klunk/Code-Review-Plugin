/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.SimpleLog4JLogSystem;

/**
 * Velocity helper methods
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class VelocityUtils {
  private VelocityUtils() {}

  public static VelocityEngine newVeloictyEngine() throws Exception {
    VelocityEngine engine = new VelocityEngine();
    engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, SimpleLog4JLogSystem.class.getName());
    engine.setProperty("runtime.log.logsystem.log4j.category", "CodeReviewPlugin");
    engine.init();
    return engine;
  }
}
