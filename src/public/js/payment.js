let contentDiv = document.querySelector("#contentDiv");
let userID = document.querySelector("#user");
let parameter = {"userID":userID.value};

$.ajax({
    url:"/payment",
    method:"POST",
    data:parameter,
    success:function(response){
        let orders = response;
        let content = "";
        content = "<table class='table table-striped'><thead class='thead-dark'><tr><th>OID</th><th>Username</th><th>Quantity</th><th class='text-center'>Products</th><th>Cost</th><th>Paid</th><th class='text-center'>Action</th></tr></thead><tbody>";
        for(let order of orders){
            if(order.paid){
                content += "<tr><td>"+order.oid+"</td><td>"+order.user.username+"</td><td>"+order.quantity+"</td><td>[ ";
                for(let product of order.products){
                    content += "[" + product.name + ", €" + product.price + "], ";
                }
                content += "]</td><td>€"+order.cost+"</td><td>"+order.paid+"</td><td><button class='btn btn-success btn-block' disabled>Paid</button></td></tr>";
            } else {
                content += "<tr><td>"+order.oid+"</td><td>"+order.user.username+"</td><td>"+order.quantity+"</td><td>[ ";
                for(let product of order.products){
                    content += "[" + product.name + ", €" + product.price + "], ";
                }
                content += "]</td><td>€"+order.cost+"</td><td>"+order.paid+"</td><td><form action='/orders/pay/"+order.oid+"' method='post'><button class='btn btn-outline-info btn-block' type='submit'>Send payment</button></form></td></tr>";
            }
        }
        content += "</tbody></table>";
        contentDiv.innerHTML = content;
    }
})