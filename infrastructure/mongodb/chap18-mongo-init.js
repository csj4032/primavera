// MongoDB initialization script for Chapter 18 - Microservices
// Creates users and sample data for product microservice

// Switch to admin database first for user creation
db = db.getSiblingDB('admin');

// Create main primavera user in admin database
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

// Create product service specific user
db.createUser({
    user: 'productservice',
    pwd: 'productservice123',
    roles: [
        {
            role: 'readWrite',
            db: 'primavera'
        }
    ]
});

// Switch to primavera database for data setup
db = db.getSiblingDB('primavera');

db.createCollection("products", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["name", "price", "category", "stock"],
            properties: {
                name: {
                    bsonType: "string",
                    description: "Product name is required and must be a string"
                },
                description: {
                    bsonType: "string",
                    description: "Product description must be a string"
                },
                price: {
                    bsonType: "number",
                    minimum: 0,
                    description: "Price is required and must be a non-negative number"
                },
                category: {
                    bsonType: "string",
                    description: "Category is required and must be a string"
                },
                stock: {
                    bsonType: "int",
                    minimum: 0,
                    description: "Stock is required and must be a non-negative integer"
                },
                status: {
                    enum: ["ACTIVE", "INACTIVE", "DISCONTINUED"],
                    description: "Status must be one of: ACTIVE, INACTIVE, DISCONTINUED"
                },
                tags: {
                    bsonType: "array",
                    items: {
                        bsonType: "string"
                    },
                    description: "Tags must be an array of strings"
                }
            }
        }
    }
});

// Insert sample product data
db.products.insertMany([
    {
        _id: "prod001",
        name: "MacBook Pro 16-inch M3",
        description: "Apple MacBook Pro with M3 Pro chip, 16-inch display",
        price: 3200000,
        category: "Electronics",
        stock: 25,
        status: "ACTIVE",
        tags: ["laptop", "apple", "premium"],
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: "prod002",
        name: "Samsung Galaxy S24 Ultra",
        description: "Samsung flagship smartphone with S Pen",
        price: 1580000,
        category: "Electronics",
        stock: 50,
        status: "ACTIVE",
        tags: ["smartphone", "samsung", "flagship"],
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: "prod003",
        name: "Sony WH-1000XM5",
        description: "Wireless noise-canceling headphones",
        price: 450000,
        category: "Audio",
        stock: 30,
        status: "ACTIVE",
        tags: ["headphones", "sony", "noise-canceling"],
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: "prod004",
        name: "iPad Air 5th Generation",
        description: "iPad Air with M1 chip and 10.9-inch display",
        price: 890000,
        category: "Electronics",
        stock: 40,
        status: "ACTIVE",
        tags: ["tablet", "apple", "ipad"],
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: "prod005",
        name: "Nintendo Switch OLED",
        description: "Nintendo Switch with OLED display",
        price: 380000,
        category: "Gaming",
        stock: 15,
        status: "INACTIVE",
        tags: ["gaming", "nintendo", "console"],
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

// Create indexes for better performance
db.products.createIndex({"category": 1});
db.products.createIndex({"status": 1});
db.products.createIndex({"price": 1});
db.products.createIndex({"tags": 1});
db.products.createIndex({"name": "text", "description": "text"});

print("MongoDB initialization completed for chap18");
print("Database: primavera");
print("Collection: products");
print("Sample products inserted: " + db.products.countDocuments());
print("Indexes created for performance optimization");