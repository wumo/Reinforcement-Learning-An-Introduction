package lab.mars.rl.util.log

import org.slf4j.Logger

inline fun Logger.info(block: () -> String) {
  if (isInfoEnabled) info(block())
}

inline fun Logger.debug(block: () -> String) {
  if (isDebugEnabled) debug(block())
}

inline fun Logger.warn(block: () -> String) {
  if (isWarnEnabled) warn(block())
}

inline fun Logger.error(block: () -> String) {
  if (isErrorEnabled) error(block())
}