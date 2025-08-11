// MongoDB initialization script for Chapter 13 - Advanced Authorization
// Creates primavera user with authentication for chap13

// Switch to admin database first
db = db.getSiblingDB('admin');

// Create primavera user in admin database for authentication
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

// Switch to primavera database
db = db.getSiblingDB('primavera');

// Create collections for logging and monitoring
db.createCollection('primavera_logs');
db.createCollection('sequences');
db.createCollection('user_activities');

// Initialize sequence for log IDs
db.sequences.insertOne({
    _id: 'primavera_logs_seq',
    value: 1
});

// Create indexes for better performance
db.primavera_logs.createIndex({ timestamp: -1 });
db.primavera_logs.createIndex({ level: 1 });
db.primavera_logs.createIndex({ logger: 1 });
db.user_activities.createIndex({ userId: 1, timestamp: -1 });

print('Chapter 13 MongoDB initialization completed successfully');