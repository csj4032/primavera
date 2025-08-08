// Create user in admin database for authentication
db = db.getSiblingDB('admin');

db.createUser({
    user: 'primavera',
    pwd: 'primavera',
    roles: [
        {
            role: 'readWrite',
            db: 'primavera'
        },
        {
            role: 'dbAdmin',
            db: 'primavera'
        }
    ]
});

// Switch to primavera database for collections
db = db.getSiblingDB('primavera');

db.users.insertMany([
    {
        _id: ObjectId(),
        email: 'admin@primavera.com',
        nickname: 'Administrator',
        status: 'ACTIVE',
        createdAt: new Date(),
        permissions: ['READ', 'WRITE', 'DELETE', 'ADMIN']
    },
    {
        _id: ObjectId(),
        email: 'manager@primavera.com',
        nickname: 'Manager',
        status: 'ACTIVE',
        createdAt: new Date(),
        permissions: ['READ', 'WRITE']
    },
    {
        _id: ObjectId(),
        email: 'user@primavera.com',
        nickname: 'User',
        status: 'ACTIVE',
        createdAt: new Date(),
        permissions: ['READ']
    }
]);

db.resources.insertMany([
    {
        _id: ObjectId(),
        name: 'User Management',
        type: 'ADMIN',
        path: '/admin/users',
        requiredPermissions: ['ADMIN']
    },
    {
        _id: ObjectId(),
        name: 'Content Management',
        type: 'CONTENT',
        path: '/content',
        requiredPermissions: ['WRITE']
    },
    {
        _id: ObjectId(),
        name: 'Public Content',
        type: 'PUBLIC',
        path: '/public',
        requiredPermissions: ['READ']
    }
]);

print('MongoDB initialized successfully for Chapter 13 - Advanced Authorization');