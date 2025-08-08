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

db = db.getSiblingDB('primavera');