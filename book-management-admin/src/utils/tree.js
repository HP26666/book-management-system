export function flattenCategoryTree(tree = [], depth = 0) {
  return tree.flatMap((node) => {
    const current = {
      ...node,
      depth,
      displayName: `${'　'.repeat(depth)}${node.name}`
    }

    return [current, ...flattenCategoryTree(node.children || [], depth + 1)]
  })
}