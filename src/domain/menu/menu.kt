package domain.menu

import lib.urlpath.UrlPath

/** An entry in a navigation menus (both the "sidebar menu" and additional "menu tabs" if any). */
data class NavigationEntry(

  /** An entry can be referred to with a key, no need to set it when never referring to it (hence nullable). */
  val key: NavigationEntryKey? = null,

  /**
   * The UI string of this entry, without a label, the entry will not be shown
   * (useful for highlighting the correct main menu items).
   */
  val uiString: String? = null,

  /** To set a CSS icon (the boolean parameter is true when this entry is active). */
  val icon: ((Boolean) -> String)? = null,

  /**
   * The primary path of this entry.
   * Used to build the link AND determine the highlighting of itself and all ancestor entries.
   */
  val urlPath: UrlPath? = null,

  /** Parameters needed by the [urlPath] for building the entry's link (usually none). */
  // TODO(cies): This could be simply a list of parameter keys (and so far we only ever use one)
  val urlPathParams: Map<String, String> = mapOf(),

  /** References to other paths based on which the highlighting of itself and all ancestors should be determined. */
  val otherUrlPaths: List<UrlPath> = listOf(),

  /**
   * List of entries that are children of this entry.
   * When a child entry is highlighted, all ancestor entries are highlighted as well.
   */
  val children: List<NavigationEntry> = listOf()
) {

  fun flattenedKeyedEntries(): Map<out NavigationEntryKey, NavigationEntry> {
    val flatMap = if (key == null) mutableMapOf() else mutableMapOf(key to this)
    children.forEach {
      it.key?.let { currentKey -> flatMap[currentKey] = it }
      flatMap.putAll(it.flattenedKeyedEntries())
    }
    return flatMap
  }

//  /** Returns the children filtered by the user's ability to see them. */
//  fun filteredChildren(user: UserEntity): List<NavigationEntry> {
//    return children.filter { it.isVisibleFor(user) }
//  }

  fun isActive(currentPath: String): Boolean {
    if (this.urlPath?.path() == currentPath) return true
    children.forEach { if (it.isActive(currentPath)) return true }
    otherUrlPaths.forEach { if (it.path() == currentPath) return true }
    return false
  }

  override fun toString(): String = "[$key]($uiString)"
}


/**
 * Enum of all navigation entry keys by which we make referring to menu entries typo-safe.
 * Only entries with children may need a key, and only if they need to be referred to.
 */
enum class NavigationEntryKey {
  Root,
  LimitedManageOrganizations,
  ManageOrganizations,
  ManageOrganizationsTab
}

val navigationTree = listOf(
  NavigationEntry(
    uiString = "Dashboard",
    icon = ::dailIcon,
    children = listOf(
      NavigationEntry(
        uiString = "First",
        urlPath = Paths.dashboard1,
        otherUrlPaths = listOf(
          Paths.dashboard
        )
      ),
      NavigationEntry(
        uiString = "Second",
        urlPath = Paths.dashboard2
      ),
      NavigationEntry(
        uiString = "Third",
        urlPath = Paths.dashboard3
      )
    )
  ),
  NavigationEntry(
    uiString = "Settings",
    icon = ::slidersIcon,
    children = listOf(
      NavigationEntry(
        uiString = "Some"
      ),
      NavigationEntry(
        uiString = "Other"
      ),
      NavigationEntry(
        uiString = "Extra"
      )
    )
  ),
)
