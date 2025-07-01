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
    icon = { isActive ->
      """
        <svg class="shrink-0 fill-current ${if (isActive) "text-violet-500" else "text-gray-400 dark:text-gray-500"}" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
          <path d="M5.936.278A7.983 7.983 0 0 1 8 0a8 8 0 1 1-8 8c0-.722.104-1.413.278-2.064a1 1 0 1 1 1.932.516A5.99 5.99 0 0 0 2 8a6 6 0 1 0 6-6c-.53 0-1.045.076-1.548.21A1 1 0 1 1 5.936.278Z" />
          <path d="M6.068 7.482A2.003 2.003 0 0 0 8 10a2 2 0 1 0-.518-3.932L3.707 2.293a1 1 0 0 0-1.414 1.414l3.775 3.775Z" />
        </svg>
      """.trimIndent()
    },
    children = listOf(
      NavigationEntry(
        uiString = "First",
        urlPath = Paths.dashboard,
        otherUrlPaths = listOf(
          Paths.reseed
        )
      ),
      NavigationEntry(
        uiString = "Second",
        urlPath = Paths.signOut
      ),
      NavigationEntry(
        uiString = "Third",
        urlPath = Paths.ping
      )
    )
  ),
  NavigationEntry(
    uiString = "Settings",
    icon = { isActive ->
      """
        <svg class="shrink-0 fill-current ${if (isActive) "text-violet-500" else "text-gray-400 dark:text-gray-500"}" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
          <path d="M10.5 1a3.502 3.502 0 0 1 3.355 2.5H15a1 1 0 1 1 0 2h-1.145a3.502 3.502 0 0 1-6.71 0H1a1 1 0 0 1 0-2h6.145A3.502 3.502 0 0 1 10.5 1ZM9 4.5a1.5 1.5 0 1 1 3 0 1.5 1.5 0 0 1-3 0ZM5.5 9a3.502 3.502 0 0 1 3.355 2.5H15a1 1 0 1 1 0 2H8.855a3.502 3.502 0 0 1-6.71 0H1a1 1 0 1 1 0-2h1.145A3.502 3.502 0 0 1 5.5 9ZM4 12.5a1.5 1.5 0 1 0 3 0 1.5 1.5 0 0 0-3 0Z" fill-rule="evenodd" />
        </svg>
      """.trimIndent()
    },
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

