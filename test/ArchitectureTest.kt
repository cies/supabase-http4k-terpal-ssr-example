import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import org.junit.jupiter.api.Test

class ArchitectureTest {

  @Test
  fun `clean architecture layers have correct dependencies`() {
    Konsist.scopeFromProduction().assertArchitecture {
      // Define layers
      // val domain = Layer("Domain", "com.myapp.domain..")
      val db = Layer("db", "db..")
      val domain = Layer("domain", "domain..")
      val filter = Layer("filter", "filter..")
      val handler = Layer("handler", "handler..")
      val html = Layer("html", "html..")
      val lib = Layer("lib", "lib..")

      // Define architecture assertions
      db.doesNotDependOn(filter, handler, html)
      domain.dependsOnNothing()
      filter.doesNotDependOn( html)
      handler.doesNotDependOn(filter)
      html.doesNotDependOn(db,filter, handler)
      lib.doesNotDependOn(db, filter, handler, html) // should not depend on "application code"
    }
  }
}
