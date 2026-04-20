export const dashboardRoute = {
  path: 'dashboard',
  name: 'Dashboard',
  component: () => import('../views/DashboardView.vue'),
  meta: {
    title: '仪表板',
    nav: true,
    icon: 'Odometer'
  }
}

export const layoutChildren = [
  dashboardRoute,
  {
    path: 'books',
    name: 'Books',
    component: () => import('../views/BooksView.vue'),
    meta: {
      title: '图书管理',
      nav: true,
      icon: 'Collection'
    }
  },
  {
    path: 'categories',
    name: 'Categories',
    component: () => import('../views/CategoriesView.vue'),
    meta: {
      title: '分类管理',
      nav: true,
      icon: 'FolderOpened',
      requiredRoles: ['admin', 'librarian']
    }
  },
  {
    path: 'users',
    name: 'Users',
    component: () => import('../views/UsersView.vue'),
    meta: {
      title: '用户管理',
      nav: true,
      icon: 'User',
      requiredRoles: ['admin', 'librarian']
    }
  },
  {
    path: 'borrow',
    name: 'Borrow',
    component: () => import('../views/BorrowView.vue'),
    meta: {
      title: '借阅管理',
      nav: true,
      icon: 'DocumentCopy'
    }
  },
  {
    path: 'reserve',
    name: 'Reserve',
    component: () => import('../views/ReserveView.vue'),
    meta: {
      title: '预约管理',
      nav: true,
      icon: 'Calendar'
    }
  },
  {
    path: 'readers',
    name: 'Readers',
    component: () => import('../views/ReadersView.vue'),
    meta: {
      title: '读者管理',
      nav: true,
      icon: 'Avatar',
      requiredRoles: ['admin', 'librarian']
    }
  },
  {
    path: 'notices',
    name: 'Notices',
    component: () => import('../views/NoticesView.vue'),
    meta: {
      title: '公告管理',
      nav: true,
      icon: 'Bell'
    }
  }
]

export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: {
      title: '登录',
      public: true
    }
  },
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      ...layoutChildren
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

export const menuRoutes = layoutChildren.filter(route => route.meta?.nav)