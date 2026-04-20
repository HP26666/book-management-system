export function flattenCategoryTree(tree, level = 0, prefix = '') {
  if (!Array.isArray(tree)) {
    return []
  }

  return tree.flatMap((item) => {
    const currentPrefix = level === 0 ? '' : `${prefix}${level > 1 ? '· ' : ''}`
    const current = {
      ...item,
      level,
      displayName: `${currentPrefix}${item.name}`
    }
    return [current, ...flattenCategoryTree(item.children || [], level + 1, `${prefix}  `)]
  })
}