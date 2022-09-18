let sortAjax = document.querySelector("#sortProductsAjax");
let contentDiv = document.querySelector("#contentDiv");
let action = document.querySelector("#action");

sortAjax.onchange = function(){
    let parameter = {"sortAjax":sortAjax.value};
    $.ajax({
        url:"/sortAjax",
        method:"POST",
        data:parameter,
        success:function(response){
            let products = response;
            let content = "";
            let condition = false;
            if(action){
                condition = true;
            }
            if(condition){
                for(let product of products){
                    content += "<div class='col-sm-4 mt-3'><div class='card bg-light border-secondary text-center'>";
                    content += "<div class='card-header border-secondary'><h3>"+product.name+"</h3></div>";
                    content += "<div class='card-body'><p class='card-text'><a href='#' data-toggle='tooltip' data-placement='top' title='"+product.info+"'>more info...</a></p></div>";
                    content += "<div class='card-footer border-secondary'><p class='card-text'>"+product.category.name+"</p></div>";
                    content += "<div class='card-footer bg-transparent border-secondary'><p class='card-text'>€"+product.price+"</p></div>";
                    content += "<div class='card-footer border-secondary text-center' id='action'><form action='/cart/add/"+product.pid+"' method='post'><button class='btn btn-outline-info' type='submit'>Add to cart</button></form></div></div></div>";
                }
            } else {
                for(let product of products){
                    content += "<div class='col-sm-4 mt-3'><div class='card bg-light border-secondary text-center'>";
                    content += "<div class='card-header border-secondary'><h3>"+product.name+"</h3></div>";
                    content += "<div class='card-body'><p class='card-text'><a href='#' data-toggle='tooltip' data-placement='top' title='"+product.info+"'>more info...</a></p></div>";
                    content += "<div class='card-footer border-secondary'><p class='card-text'>"+product.category.name+"</p></div>";
                    content += "<div class='card-footer bg-transparent border-secondary'><p class='card-text'>€"+product.price+"</p></div></div></div>";
                }
            }
            contentDiv.innerHTML = content;
        }
    })
}
