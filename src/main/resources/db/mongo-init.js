db.createCollection("payments", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      title: "Payment",
      required: [
        "concepto",
        "cantidadProductos",
        "pagador",
        "beneficiario",
        "montoTotal",
        "estatus",
        "fechaCreacion"
      ],
      properties: {
        _id: { bsonType: "objectId" },
        concepto: {
          bsonType: "string",
          minLength: 1,
          maxLength: 255
        },
        cantidadProductos: {
          bsonType: "int",
          minimum: 1
        },
        pagador: {
          bsonType: "string",
          minLength: 1,
          maxLength: 150
        },
        beneficiario: {
          bsonType: "string",
          minLength: 1,
          maxLength: 150
        },
        montoTotal: {
          bsonType: "decimal",
          minimum: 0.01
        },
        estatus: {
          bsonType: "string",
          enum: ["PENDING", "PROCESSING", "COMPLETED", "FAILED"]
        },
        fechaCreacion: { bsonType: "date" },
        fechaActualizacion: { bsonType: "date" }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "warn"
});

db.payments.createIndex({ estatus: 1 });
db.payments.createIndex({ pagador: 1 });
db.payments.createIndex({ beneficiario: 1 });
db.payments.createIndex({ fechaCreacion: -1 });

print("Colección 'payments' creada con validación.");