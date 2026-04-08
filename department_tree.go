package main

import (
	"encoding/json"
	"fmt"
	"sort"
	"sync"
	"time"
)

// Department 表示部门节点
type Department struct {
	ID         int64          `json:"id"`
	Name       string         `json:"name"`
	ParentID   int64          `json:"parent_id"`
	Level      int            `json:"level"`
	Children   []*Department  `json:"children,omitempty"`
	CreatedAt  time.Time      `json:"created_at"`
	UpdatedAt  time.Time      `json:"updated_at"`
}

// DepartmentTree 部门树结构，支持并发安全操作
type DepartmentTree struct {
	mu           sync.RWMutex
	nodes        map[int64]*Department
	childrenMap  map[int64][]*Department
	rootIDs      []int64
	isBuilt      bool
}

// NewDepartmentTree 创建新的部门树
func NewDepartmentTree() *DepartmentTree {
	return &DepartmentTree{
		nodes:       make(map[int64]*Department),
		childrenMap: make(map[int64][]*Department),
		rootIDs:     make([]int64, 0),
	}
}

// Build 从扁平列表构建树结构，时间复杂度 O(n)
func (dt *DepartmentTree) Build(depts []*Department) error {
	dt.mu.Lock()
	defer dt.mu.Unlock()

	// 清空现有数据
	dt.nodes = make(map[int64]*Department, len(depts))
	dt.childrenMap = make(map[int64][]*Department, len(depts))
	dt.rootIDs = make([]int64, 0)
	dt.isBuilt = false

	// 验证并存储所有节点
	for _, dept := range depts {
		if dept == nil {
			return fmt.Errorf("department cannot be nil")
		}
		if dept.ID <= 0 {
			return fmt.Errorf("invalid department ID: %d", dept.ID)
		}
		if dept.Name == "" {
			return fmt.Errorf("department name cannot be empty for ID: %d", dept.ID)
		}
		if _, exists := dt.nodes[dept.ID]; exists {
			return fmt.Errorf("duplicate department ID: %d", dept.ID)
		}
		
		// 创建副本避免外部修改
		deptCopy := &Department{
			ID:        dept.ID,
			Name:      dept.Name,
			ParentID:  dept.ParentID,
			Level:     dept.Level,
			CreatedAt: dept.CreatedAt,
			UpdatedAt: dept.UpdatedAt,
		}
		dt.nodes[dept.ID] = deptCopy
	}

	// 构建父子关系
	for _, dept := range dt.nodes {
		if dept.ParentID == 0 || dept.ParentID == dept.ID {
			// 根节点
			dt.rootIDs = append(dt.rootIDs, dept.ID)
		} else {
			// 检查父节点是否存在
			if _, exists := dt.nodes[dept.ParentID]; !exists {
				return fmt.Errorf("parent department %d not found for department %d", dept.ParentID, dept.ID)
			}
			dt.childrenMap[dept.ParentID] = append(dt.childrenMap[dept.ParentID], dept)
		}
	}

	// 对子节点按ID排序以保证一致性
	for parentID := range dt.childrenMap {
		sort.Slice(dt.childrenMap[parentID], func(i, j int) bool {
			return dt.childrenMap[parentID][i].ID < dt.childrenMap[parentID][j].ID
		})
	}

	// 检测循环依赖
	if err := dt.detectCycle(); err != nil {
		return err
	}

	// 计算层级
	dt.calculateLevels()

	dt.isBuilt = true
	return nil
}

// detectCycle 检测是否存在循环依赖
func (dt *DepartmentTree) detectCycle() error {
	visited := make(map[int64]bool)
	recStack := make(map[int64]bool)

	var dfs func(id int64) error
	dfs = func(id int64) error {
		visited[id] = true
		recStack[id] = true

		for _, child := range dt.childrenMap[id] {
			if !visited[child.ID] {
				if err := dfs(child.ID); err != nil {
					return err
				}
			} else if recStack[child.ID] {
				return fmt.Errorf("cycle detected involving department %d", child.ID)
			}
		}

		recStack[id] = false
		return nil
	}

	for _, rootID := range dt.rootIDs {
		if !visited[rootID] {
			if err := dfs(rootID); err != nil {
				return err
			}
		}
	}

	return nil
}

// calculateLevels 计算每个节点的层级
func (dt *DepartmentTree) calculateLevels() {
	var dfs func(id int64, level int)
	dfs = func(id int64, level int) {
		node := dt.nodes[id]
		node.Level = level
		for _, child := range dt.childrenMap[id] {
			dfs(child.ID, level+1)
		}
	}

	for _, rootID := range dt.rootIDs {
		dfs(rootID, 1)
	}
}

// GetTree 获取完整的树结构
func (dt *DepartmentTree) GetTree() []*Department {
	dt.mu.RLock()
	defer dt.mu.RUnlock()

	if !dt.isBuilt {
		return nil
	}

	result := make([]*Department, 0, len(dt.rootIDs))
	for _, rootID := range dt.rootIDs {
		result = append(result, dt.buildSubTree(rootID))
	}

	return result
}

// buildSubTree 递归构建子树
func (dt *DepartmentTree) buildSubTree(id int64) *Department {
	node := dt.nodes[id]
	children := dt.childrenMap[id]

	if len(children) == 0 {
		// 叶子节点，返回不带children字段的副本
		return &Department{
			ID:        node.ID,
			Name:      node.Name,
			ParentID:  node.ParentID,
			Level:     node.Level,
			CreatedAt: node.CreatedAt,
			UpdatedAt: node.UpdatedAt,
		}
	}

	childNodes := make([]*Department, 0, len(children))
	for _, child := range children {
		childNodes = append(childNodes, dt.buildSubTree(child.ID))
	}

	return &Department{
		ID:        node.ID,
		Name:      node.Name,
		ParentID:  node.ParentID,
		Level:     node.Level,
		Children:  childNodes,
		CreatedAt: node.CreatedAt,
		UpdatedAt: node.UpdatedAt,
	}
}

// GetNode 获取单个节点信息
func (dt *DepartmentTree) GetNode(id int64) (*Department, bool) {
	dt.mu.RLock()
	defer dt.mu.RUnlock()

	node, exists := dt.nodes[id]
	if !exists {
		return nil, false
	}

	// 返回副本
	return &Department{
		ID:        node.ID,
		Name:      node.Name,
		ParentID:  node.ParentID,
		Level:     node.Level,
		CreatedAt: node.CreatedAt,
		UpdatedAt: node.UpdatedAt,
	}, true
}

// GetChildren 获取指定部门的直接子部门
func (dt *DepartmentTree) GetChildren(parentID int64) []*Department {
	dt.mu.RLock()
	defer dt.mu.RUnlock()

	children := dt.childrenMap[parentID]
	if len(children) == 0 {
		return []*Department{}
	}

	result := make([]*Department, 0, len(children))
	for _, child := range children {
		result = append(result, &Department{
			ID:        child.ID,
			Name:      child.Name,
			ParentID:  child.ParentID,
			Level:     child.Level,
			CreatedAt: child.CreatedAt,
			UpdatedAt: child.UpdatedAt,
		})
	}

	return result
}

// GetAncestors 获取指定部门的所有祖先节点（从直接父节点到根节点）
func (dt *DepartmentTree) GetAncestors(id int64) ([]*Department, error) {
	dt.mu.RLock()
	defer dt.mu.RUnlock()

	node, exists := dt.nodes[id]
	if !exists {
		return nil, fmt.Errorf("department %d not found", id)
	}

	ancestors := make([]*Department, 0)
	currentParentID := node.ParentID

	// 防止无限循环的安全检查
	visited := make(map[int64]bool)
	visited[id] = true

	for currentParentID != 0 && currentParentID != id {
		if visited[currentParentID] {
			return nil, fmt.Errorf("cycle detected while fetching ancestors")
		}
		visited[currentParentID] = true

		parent, exists := dt.nodes[currentParentID]
		if !exists {
			break
		}

		ancestors = append(ancestors, &Department{
			ID:        parent.ID,
			Name:      parent.Name,
			ParentID:  parent.ParentID,
			Level:     parent.Level,
			CreatedAt: parent.CreatedAt,
			UpdatedAt: parent.UpdatedAt,
		})

		currentParentID = parent.ParentID
	}

	return ancestors, nil
}

// GetPath 获取从根节点到指定节点的完整路径
func (dt *DepartmentTree) GetPath(id int64) ([]*Department, error) {
	ancestors, err := dt.GetAncestors(id)
	if err != nil {
		return nil, err
	}

	node, exists := dt.GetNode(id)
	if !exists {
		return nil, fmt.Errorf("department %d not found", id)
	}

	// 反转祖先列表并添加当前节点
	path := make([]*Department, 0, len(ancestors)+1)
	for i := len(ancestors) - 1; i >= 0; i-- {
		path = append(path, ancestors[i])
	}
	path = append(path, node)

	return path, nil
}

// Size 返回部门总数
func (dt *DepartmentTree) Size() int {
	dt.mu.RLock()
	defer dt.mu.RUnlock()
	return len(dt.nodes)
}

// MarshalJSON 实现JSON序列化
func (dt *DepartmentTree) MarshalJSON() ([]byte, error) {
	dt.mu.RLock()
	defer dt.mu.RUnlock()

	if !dt.isBuilt {
		return json.Marshal([]interface{}{})
	}

	tree := make([]*Department, 0, len(dt.rootIDs))
	for _, rootID := range dt.rootIDs {
		tree = append(tree, dt.buildSubTree(rootID))
	}

	return json.Marshal(tree)
}

// 示例使用
func main() {
	// 创建测试数据
	now := time.Now()
	depts := []*Department{
		{ID: 1, Name: "总公司", ParentID: 0, CreatedAt: now, UpdatedAt: now},
		{ID: 2, Name: "研发中心", ParentID: 1, CreatedAt: now, UpdatedAt: now},
		{ID: 3, Name: "市场部", ParentID: 1, CreatedAt: now, UpdatedAt: now},
		{ID: 4, Name: "后端组", ParentID: 2, CreatedAt: now, UpdatedAt: now},
		{ID: 5, Name: "前端组", ParentID: 2, CreatedAt: now, UpdatedAt: now},
		{ID: 6, Name: "产品部", ParentID: 2, CreatedAt: now, UpdatedAt: now},
		{ID: 7, Name: "华东区", ParentID: 3, CreatedAt: now, UpdatedAt: now},
		{ID: 8, Name: "华北区", ParentID: 3, CreatedAt: now, UpdatedAt: now},
	}

	// 创建部门树
	tree := NewDepartmentTree()

	// 构建树
	if err := tree.Build(depts); err != nil {
		fmt.Printf("Build error: %v\n", err)
		return
	}

	// 获取完整树
	fullTree := tree.GetTree()
	jsonData, _ := json.MarshalIndent(fullTree, "", "  ")
	fmt.Println("完整部门树:")
	fmt.Println(string(jsonData))

	// 获取单个节点
	if node, ok := tree.GetNode(4); ok {
		fmt.Printf("\n节点4: %+v\n", node)
	}

	// 获取子节点
	children := tree.GetChildren(2)
	fmt.Printf("\n研发中心的子部门数: %d\n", len(children))

	// 获取祖先节点
	ancestors, _ := tree.GetAncestors(4)
	fmt.Printf("\n后端组的祖先数: %d\n", len(ancestors))

	// 获取完整路径
	path, _ := tree.GetPath(4)
	fmt.Printf("\n后端组的完整路径长度: %d\n", len(path))

	fmt.Printf("\n部门总数: %d\n", tree.Size())
}
