package com.example

import com.natpryce.krouton.ROOT
import com.natpryce.krouton.div

data object Paths {
  val ping =
    ROOT / "ping"
  val register =
    ROOT / "register"
  val metrics =
    ROOT / "metrics"
  val jdbi =
    ROOT / "jdbi"
  val oAuthRoot =
    ROOT / "oauth"
  val oAuthCallback =
    ROOT / "oauth" / "callback"
}
