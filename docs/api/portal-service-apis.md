# 门户展示层接口设计文档

## 1. 模块概述

门户展示层负责平台的公开展示内容，包括首页、文档中心、订阅计划展示、产品介绍等功能。这些接口主要面向匿名用户和已登录用户的信息浏览需求。

### 关联数据库表
- `portal_content` - 门户内容管理表
- `subscription_plans` - 订阅计划表
- `users` - 用户表（用于个性化展示）
- `user_subscriptions` - 用户订阅表（用于订阅状态展示）

## 2. 首页内容接口

### 2.1 获取首页内容

**接口路径**: `GET /api/portal/home`

**请求头**:
```
# 可选认证，用于个性化展示
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (可选)
```

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "hero": {
      "title": "智能体服务平台",
      "subtitle": "构建、部署和管理您的AI智能体",
      "description": "通过我们的平台，您可以轻松创建强大的AI智能体，处理各种业务场景",
      "ctaText": "立即开始",
      "ctaLink": "/register",
      "backgroundImage": "/images/hero-bg.jpg"
    },
    "features": [
      {
        "icon": "🤖",
        "title": "智能体管理",
        "description": "创建和管理多个AI智能体，支持不同的业务场景"
      },
      {
        "icon": "📊",
        "title": "数据处理",
        "description": "强大的数据处理能力，支持多种格式的数据导入和处理"
      },
      {
        "icon": "🔧",
        "title": "模型配置",
        "description": "灵活的模型配置选项，支持多种AI模型供应商"
      },
      {
        "icon": "📈",
        "title": "实时监控",
        "description": "全面的使用统计和性能监控，帮助优化AI服务"
      }
    ],
    "statistics": {
      "totalUsers": 10000,
      "totalAgents": 25000,
      "totalApiCalls": 5000000,
      "uptime": "99.9%"
    },
    "testimonials": [
      {
        "name": "张三",
        "company": "科技公司CEO",
        "content": "这个平台帮助我们快速构建了客服智能体，大大提升了客户服务效率。",
        "avatar": "/images/avatar1.jpg"
      }
    ],
    "pricing": {
      "showPricing": true,
      "ctaText": "查看定价",
      "ctaLink": "/pricing"
    }
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |

### 2.2 获取产品特性详情

**接口路径**: `GET /api/portal/features`

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "categories": [
      {
        "categoryId": "core-features",
        "title": "核心功能",
        "description": "平台的核心AI智能体功能",
        "features": [
          {
            "featureId": "agent-management",
            "title": "智能体管理",
            "description": "创建、配置和管理多个AI智能体实例",
            "icon": "🤖",
            "benefits": [
              "支持多种AI模型",
              "灵活的配置选项",
              "实时性能监控"
            ],
            "demoUrl": "/demo/agent-management"
          },
          {
            "featureId": "data-processing",
            "title": "数据处理",
            "description": "强大的数据导入、处理和管理能力",
            "icon": "📊",
            "benefits": [
              "多格式数据支持",
              "自动数据清洗",
              "向量化处理"
            ],
            "demoUrl": "/demo/data-processing"
          }
        ]
      },
      {
        "categoryId": "integration",
        "title": "集成能力",
        "description": "与第三方系统的集成功能",
        "features": [
          {
            "featureId": "api-integration",
            "title": "API集成",
            "description": "RESTful API支持，轻松集成到现有系统",
            "icon": "🔌",
            "benefits": [
              "标准REST API",
              "SDK支持",
              "Webhook通知"
            ],
            "demoUrl": "/demo/api-integration"
          }
        ]
      }
    ]
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |

## 3. 订阅计划展示接口

### 3.1 获取订阅计划列表

**接口路径**: `GET /api/portal/pricing`

**请求头**:
```
# 可选认证，用于显示当前订阅状态
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (可选)
```

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "plans": [
      {
        "planId": "plan-free",
        "name": "免费版",
        "description": "适合个人用户和小型项目",
        "price": {
          "monthly": 0,
          "yearly": 0,
          "currency": "CNY"
        },
        "features": [
          "1个智能体",
          "1000次API调用/月",
          "基础数据处理",
          "社区支持"
        ],
        "limitations": [
          "不支持自定义模型",
          "有限的数据存储"
        ],
        "popular": false,
        "ctaText": "免费开始",
        "ctaLink": "/register"
      },
      {
        "planId": "plan-pro",
        "name": "专业版",
        "description": "适合中小企业和专业开发者",
        "price": {
          "monthly": 99,
          "yearly": 990,
          "currency": "CNY"
        },
        "features": [
          "20个智能体",
          "10万次API调用/月",
          "高级数据处理",
          "优先技术支持",
          "自定义模型配置",
          "数据导出功能"
        ],
        "limitations": [],
        "popular": true,
        "ctaText": "选择专业版",
        "ctaLink": "/subscribe/pro",
        "discount": {
          "type": "yearly",
          "percentage": 17,
          "description": "年付享83折优惠"
        }
      },
      {
        "planId": "plan-enterprise",
        "name": "企业版",
        "description": "适合大型企业和高频使用场景",
        "price": {
          "monthly": 999,
          "yearly": 9990,
          "currency": "CNY"
        },
        "features": [
          "无限智能体",
          "100万次API调用/月",
          "企业级数据处理",
          "专属客户经理",
          "私有化部署选项",
          "SLA保障",
          "定制开发支持"
        ],
        "limitations": [],
        "popular": false,
        "ctaText": "联系销售",
        "ctaLink": "/contact-sales"
      }
    ],
    "currentUserPlan": {
      "planId": "plan-pro",
      "name": "专业版",
      "status": "active",
      "expiresAt": "2024-07-20T00:00:00Z",
      "usage": {
        "agents": {
          "used": 5,
          "limit": 20
        },
        "apiCalls": {
          "used": 25000,
          "limit": 100000,
          "resetDate": "2024-02-01T00:00:00Z"
        }
      }
    },
    "faq": [
      {
        "question": "可以随时升级或降级订阅吗？",
        "answer": "是的，您可以随时在账户设置中升级或降级您的订阅计划。升级立即生效，降级将在当前计费周期结束后生效。"
      },
      {
        "question": "API调用次数如何计算？",
        "answer": "每次向智能体发送消息或调用API接口都会计为一次API调用。系统会实时统计您的使用量。"
      }
    ]
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |

### 3.2 获取计划对比详情

**接口路径**: `GET /api/portal/pricing/comparison`

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "comparisonTable": {
      "categories": [
        {
          "categoryName": "基础功能",
          "features": [
            {
              "featureName": "智能体数量",
              "free": "1个",
              "pro": "20个",
              "enterprise": "无限"
            },
            {
              "featureName": "API调用次数/月",
              "free": "1,000次",
              "pro": "100,000次",
              "enterprise": "1,000,000次"
            },
            {
              "featureName": "数据存储",
              "free": "100MB",
              "pro": "10GB",
              "enterprise": "无限"
            }
          ]
        },
        {
          "categoryName": "高级功能",
          "features": [
            {
              "featureName": "自定义模型",
              "free": "❌",
              "pro": "✅",
              "enterprise": "✅"
            },
            {
              "featureName": "数据导出",
              "free": "❌",
              "pro": "✅",
              "enterprise": "✅"
            },
            {
              "featureName": "私有化部署",
              "free": "❌",
              "pro": "❌",
              "enterprise": "✅"
            }
          ]
        },
        {
          "categoryName": "支持服务",
          "features": [
            {
              "featureName": "技术支持",
              "free": "社区支持",
              "pro": "优先支持",
              "enterprise": "专属客户经理"
            },
            {
              "featureName": "SLA保障",
              "free": "❌",
              "pro": "99.5%",
              "enterprise": "99.9%"
            }
          ]
        }
      ]
    }
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |

## 4. 文档中心接口

### 4.1 获取文档分类列表

**接口路径**: `GET /api/portal/docs/categories`

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "categoryId": "getting-started",
      "name": "快速开始",
      "description": "平台入门指南和基础教程",
      "icon": "🚀",
      "order": 1,
      "documentCount": 8
    },
    {
      "categoryId": "api-reference",
      "name": "API参考",
      "description": "完整的API接口文档",
      "icon": "📚",
      "order": 2,
      "documentCount": 25
    },
    {
      "categoryId": "tutorials",
      "name": "教程指南",
      "description": "详细的使用教程和最佳实践",
      "icon": "📖",
      "order": 3,
      "documentCount": 15
    },
    {
      "categoryId": "faq",
      "name": "常见问题",
      "description": "用户常见问题解答",
      "icon": "❓",
      "order": 4,
      "documentCount": 20
    }
  ]
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |

### 4.2 获取文档列表

**接口路径**: `GET /api/portal/docs`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| categoryId | String | 否 | 分类ID筛选 | getting-started |
| search | String | 否 | 搜索关键词 | API |
| page | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页数量，默认10 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 68,
    "page": 1,
    "size": 10,
    "items": [
      {
        "documentId": "doc-001",
        "title": "平台介绍",
        "summary": "了解智能体服务平台的核心功能和优势",
        "categoryId": "getting-started",
        "categoryName": "快速开始",
        "tags": ["介绍", "概述"],
        "readTime": 5,
        "difficulty": "beginner",
        "lastUpdated": "2024-01-20T10:00:00Z",
        "viewCount": 1250,
        "slug": "platform-introduction"
      },
      {
        "documentId": "doc-002",
        "title": "创建第一个智能体",
        "summary": "step-by-step指南，帮助您创建第一个AI智能体",
        "categoryId": "getting-started",
        "categoryName": "快速开始",
        "tags": ["教程", "智能体"],
        "readTime": 10,
        "difficulty": "beginner",
        "lastUpdated": "2024-01-19T15:30:00Z",
        "viewCount": 980,
        "slug": "create-first-agent"
      }
    ]
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 400 | 参数错误 |
| 500 | 服务器内部错误 |

### 4.3 获取文档内容

**接口路径**: `GET /api/portal/docs/{documentId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| documentId | String | 是 | 文档ID或slug | doc-001 或 platform-introduction |

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "documentId": "doc-001",
    "title": "平台介绍",
    "summary": "了解智能体服务平台的核心功能和优势",
    "content": "# 平台介绍\n\n欢迎使用智能体服务平台...\n\n## 核心功能\n\n1. **智能体管理**\n   - 创建和配置AI智能体\n   - 支持多种AI模型\n\n2. **数据处理**\n   - 数据导入和清洗\n   - 向量化处理\n\n...",
    "categoryId": "getting-started",
    "categoryName": "快速开始",
    "tags": ["介绍", "概述"],
    "readTime": 5,
    "difficulty": "beginner",
    "lastUpdated": "2024-01-20T10:00:00Z",
    "viewCount": 1251,
    "slug": "platform-introduction",
    "tableOfContents": [
      {
        "level": 1,
        "title": "平台介绍",
        "anchor": "platform-introduction"
      },
      {
        "level": 2,
        "title": "核心功能",
        "anchor": "core-features"
      },
      {
        "level": 3,
        "title": "智能体管理",
        "anchor": "agent-management"
      }
    ],
    "relatedDocs": [
      {
        "documentId": "doc-002",
        "title": "创建第一个智能体",
        "slug": "create-first-agent"
      },
      {
        "documentId": "doc-003",
        "title": "API快速开始",
        "slug": "api-quickstart"
      }
    ]
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 404 | 文档不存在 |
| 500 | 服务器内部错误 |

### 4.4 搜索文档

**接口路径**: `GET /api/portal/docs/search`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| q | String | 是 | 搜索关键词 | API接口 |
| categoryId | String | 否 | 分类ID筛选 | api-reference |
| page | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页数量，默认10 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": {
    "query": "API接口",
    "total": 15,
    "page": 1,
    "size": 10,
    "items": [
      {
        "documentId": "doc-010",
        "title": "API接口概览",
        "summary": "平台提供的所有API接口的概览和使用说明",
        "categoryName": "API参考",
        "highlights": [
          "平台提供了完整的<mark>API接口</mark>文档",
          "所有<mark>API接口</mark>都支持RESTful规范"
        ],
        "relevanceScore": 0.95,
        "slug": "api-overview"
      }
    ],
    "suggestions": [
      "API文档",
      "接口调用",
      "API认证"
    ]
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 400 | 搜索关键词不能为空 |
| 500 | 服务器内部错误 |

## 5. 公告和新闻接口

### 5.1 获取公告列表

**接口路径**: `GET /api/portal/announcements`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| type | String | 否 | 公告类型：system/feature/maintenance | system |
| page | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页数量，默认5 | 5 |

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 12,
    "page": 1,
    "size": 5,
    "items": [
      {
        "announcementId": "ann-001",
        "title": "平台升级公告",
        "summary": "我们将在1月25日进行系统升级，预计维护时间2小时",
        "content": "详细的升级内容和影响说明...",
        "type": "maintenance",
        "priority": "high",
        "status": "active",
        "publishedAt": "2024-01-20T09:00:00Z",
        "expiresAt": "2024-01-26T00:00:00Z",
        "tags": ["升级", "维护"]
      },
      {
        "announcementId": "ann-002",
        "title": "新功能发布：智能体模板",
        "summary": "我们推出了智能体模板功能，帮助用户快速创建常用类型的智能体",
        "content": "功能详细介绍...",
        "type": "feature",
        "priority": "medium",
        "status": "active",
        "publishedAt": "2024-01-18T14:00:00Z",
        "expiresAt": null,
        "tags": ["新功能", "智能体"]
      }
    ]
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 400 | 参数错误 |
| 500 | 服务器内部错误 |

### 5.2 获取公告详情

**接口路径**: `GET /api/portal/announcements/{announcementId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| announcementId | String | 是 | 公告ID | ann-001 |

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "announcementId": "ann-001",
    "title": "平台升级公告",
    "summary": "我们将在1月25日进行系统升级，预计维护时间2小时",
    "content": "# 平台升级公告\n\n## 升级时间\n\n2024年1月25日 02:00 - 04:00 (UTC+8)\n\n## 升级内容\n\n1. 性能优化\n2. 新增智能体模板功能\n3. 修复已知问题\n\n## 影响范围\n\n- API服务将暂时不可用\n- 用户无法登录平台\n\n## 注意事项\n\n请在升级前保存好您的工作，避免数据丢失。",
    "type": "maintenance",
    "priority": "high",
    "status": "active",
    "publishedAt": "2024-01-20T09:00:00Z",
    "expiresAt": "2024-01-26T00:00:00Z",
    "tags": ["升级", "维护"],
    "viewCount": 856
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 404 | 公告不存在 |
| 500 | 服务器内部错误 |

## 6. 联系和支持接口

### 6.1 提交联系表单

**接口路径**: `POST /api/portal/contact`

**请求参数**:
```json
{
  "name": "张三",
  "email": "zhangsan@example.com",
  "company": "科技公司",
  "phone": "13800138000",
  "subject": "企业版咨询",
  "message": "我们想了解企业版的详细功能和定价信息",
  "type": "sales"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| name | String | 是 | 联系人姓名 | 张三 |
| email | String | 是 | 邮箱地址 | zhangsan@example.com |
| company | String | 否 | 公司名称 | 科技公司 |
| phone | String | 否 | 电话号码 | 13800138000 |
| subject | String | 是 | 主题 | 企业版咨询 |
| message | String | 是 | 详细信息 | 我们想了解企业版的详细功能... |
| type | String | 是 | 类型：sales/support/feedback/other | sales |

**响应结果**:
```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "contactId": "contact-550e8400-e29b-41d4-a716-446655440000",
    "status": "submitted",
    "estimatedResponseTime": "24小时内",
    "submittedAt": "2024-01-20T16:30:00Z"
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 400 | 参数错误或邮箱格式不正确 |
| 429 | 提交过于频繁，请稍后再试 |
| 500 | 服务器内部错误 |

### 6.2 获取支持信息

**接口路径**: `GET /api/portal/support`

**响应结果**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "channels": [
      {
        "type": "email",
        "name": "邮件支持",
        "value": "support@example.com",
        "description": "技术问题和一般咨询",
        "responseTime": "24小时内",
        "available": "7x24"
      },
      {
        "type": "phone",
        "name": "电话支持",
        "value": "400-123-4567",
        "description": "紧急问题和企业客户",
        "responseTime": "立即",
        "available": "工作日 9:00-18:00"
      },
      {
        "type": "chat",
        "name": "在线客服",
        "value": "/chat",
        "description": "实时在线咨询",
        "responseTime": "立即",
        "available": "工作日 9:00-18:00"
      }
    ],
    "faq": {
      "title": "常见问题",
      "link": "/docs/faq",
      "description": "查看用户最常遇到的问题和解答"
    },
    "documentation": {
      "title": "帮助文档",
      "link": "/docs",
      "description": "完整的产品使用指南和API文档"
    },
    "status": {
      "title": "服务状态",
      "link": "/status",
      "description": "查看平台实时运行状态",
      "currentStatus": "正常运行"
    }
  }
}
```

**错误码**:
| 错误码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |

## 7. 通用响应格式

所有接口均采用统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-20T16:30:00Z"
}
```

**字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 响应消息 |
| data | Object/Array/null | 响应数据 |
| timestamp | String | 响应时间戳 |

## 8. 认证与权限

### 认证机制
- 大部分门户接口无需认证，可匿名访问
- 部分接口支持可选认证，用于个性化展示
- 认证通过`Authorization: Bearer {token}`头部传递

### 缓存策略
- 静态内容（文档、公告等）采用CDN缓存
- 动态内容（用户相关信息）实时获取
- 合理的缓存TTL设置，平衡性能和实时性

### 限流规则
- 匿名用户：100次/分钟
- 已认证用户：500次/分钟
- 联系表单提交：5次/小时