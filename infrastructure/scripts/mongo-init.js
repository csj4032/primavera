// MongoDB initialization script for Primavera project
// Creates primavera user with readWrite permissions

db = db.getSiblingDB('primavera');

// Create primavera user
db.createUser({
  user: 'primavera',
  pwd: 'primavera',
  roles: [
    {
      role: 'readWrite',
      db: 'primavera'
    }
  ]
});

// Create initial collections if needed
db.createCollection('users');
db.createCollection('roles');
db.createCollection('user_roles');

print('Primavera MongoDB user and database initialized successfully');