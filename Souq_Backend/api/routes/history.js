const express = require('express')
const router = express.Router()

// import file
const database = require("../../config")

// Get All recently_viewed products
router.get("/", (request, response) => {
    var userId = request.query.userId;
    var page = request.query.page;
    var page_size = request.query.page_size;

    console.log(typeof page);

    if(page == null){
        page = 0;
     }
 
     if(page_size == null){
        page_size = 25;
     }

     const args = [
        userId,
        parseInt(page_size),
        parseInt(page)
    ];

    const query = "SELECT product.name, product.price, product.image, product.category, product.quantity, product.supplier FROM History JOIN Product JOIN User ON history.product_id = product.id AND history.user_id = user.id WHERE user_id = ? LIMIT ? OFFSET ?"
    database.query(query, args, (error, result) => {
        if(error) throw error;
        response.status(200).json({
            "history" : result
        })

    })
});

// Add to History
router.post("/add", (request, response) => {
    const userId = request.body.userId
    const productId = request.body.productId
  
    const query = "INSERT INTO history(user_Id, product_Id) VALUES(?, ?)"
   
    const args = [userId, productId]

    database.query(query, args, (error, result) => {
        if(error) throw error
        response.status(200).send("Added to History")
    });
});
      
module.exports = router