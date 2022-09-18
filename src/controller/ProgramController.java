package controller;

import com.google.gson.Gson;
import model.*;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.*;

import static spark.Spark.*;

public class ProgramController {

    public static void main(String[] args) {
        staticFiles.location("/public");
        port(5000);
        String userJson = "user.json";
        String orderJson = "order.json";
        String cartJson = "cart.json";
        String categoryJson = "category.json";
        String productJson = "product.json";
        HashMap<String, Object> dataMap = new HashMap<>();
        final ArrayList[] toSortProductSearch = new ArrayList[]{new ArrayList<>()};

        get("/", (request, response) -> {
            dataMap.put("noUser", "noUser");
            if (dataMap.containsKey("userStatus")) {
                dataMap.remove("noUser");
                if (request.session().attribute("userStatus").toString().equals("admin")) {
                    dataMap.put("userAdmin", "userAdmin");
                } else {
                    dataMap.put("userUser", "userUser");
                }
            }
            if (dataMap.containsKey("productSearch")) {
                dataMap.put("products", dataMap.get("productSearch"));
            } else if (dataMap.containsKey("sortedProducts")) {
                dataMap.put("products", dataMap.get("sortedProducts"));
            } else if (dataMap.containsKey("messageSearchEmpty") || dataMap.containsKey("messageSearchNotFound")) {
                dataMap.remove("products");
            } else {
                dataMap.put("products", ProductJsonActions.readFromJson(productJson));
            }
            dataMap.put("clear", "clear");
//            ArrayList<User> users = new ArrayList<>();
//            User user = new User("blef", "blef", "admin");
//            users.add(user);
//            UserJsonActions.writeToJSON(users, userJson);
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "index.hbs"));
        });

        post("/search", (request, response) -> {
            String term = request.queryParams("searchBox");
            term = term.toLowerCase().trim();
            dataMap.put("term", term);
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            ArrayList<Product> productSearch = new ArrayList<>();
            if (!term.isEmpty()) {
                for (Product product : products) {
                    String tmpName = product.getName().toLowerCase();
                    String tmpInfo = product.getInfo().toLowerCase();
                    if (tmpName.contains(term) || tmpInfo.contains(term)) {
                        productSearch.add(product);
                    }
                }
            }
            if (productSearch.size() > 0) {
                toSortProductSearch[0] = productSearch;
                dataMap.put("productSearch", productSearch);
                if (productSearch.size() < 2) {
                    dataMap.put("messageSearchFound", "We found [" + productSearch.size() + "] product that match your search criteria.");
                } else {
                    dataMap.put("messageSearchFound", "We found [" + productSearch.size() + "] products that match your search criteria.");
                }
                dataMap.remove("messageSearchEmpty");
                dataMap.remove("messageSearchNotFound");
            } else {
                if (term.isEmpty()) {
                    dataMap.put("messageSearchEmpty", "You didn't set any search criteria.");
                    dataMap.remove("messageSearchNotFound");
                } else {
                    dataMap.put("messageSearchNotFound", "Searching didn't match any criteria. You searched for: {" + term + "}");
                    dataMap.remove("messageSearchEmpty");
                }
            }
            response.redirect("/");
            return null;
        });

        post("/sort", (request, response) -> {
            String sort = request.queryParams("sortProducts");
            ArrayList<Product> products;
            if (dataMap.containsKey("productSearch")) {
                products = toSortProductSearch[0];
            } else {
                products = ProductJsonActions.readFromJson(productJson);
            }
            ArrayList<Product> sortedProducts = new ArrayList<>();
            switch (sort) {
                case "nameAsc":
                    products.sort((product1, product2) -> product1.getName().compareToIgnoreCase(product2.getName()));
                    sortedProducts = products;
                    dataMap.put("sortBy", "nameAsc");
                    dataMap.put("sort", "Name ascending");
                    break;
                case "nameDesc":
                    products.sort((product1, product2) -> product2.getName().compareToIgnoreCase(product1.getName()));
                    sortedProducts = products;
                    dataMap.put("sortBy", "nameDesc");
                    dataMap.put("sort", "Name descending");
                    break;
                case "priceAsc":
                    products.sort(Comparator.comparingDouble(Product::getPrice));
                    sortedProducts = products;
                    dataMap.put("sortBy", "priceAsc");
                    dataMap.put("sort", "Price ascending");
                    break;
                case "priceDesc":
                    products.sort(Collections.reverseOrder(Comparator.comparingDouble(Product::getPrice)));
                    sortedProducts = products;
                    dataMap.put("sortBy", "priceDesc");
                    dataMap.put("sort", "Price descending");
                    break;
                case "default":
                    sortedProducts = products;
                    dataMap.remove("sortBy");
                    break;
            }
            dataMap.remove("clear");
            dataMap.put("sortedProducts", sortedProducts);
            response.redirect("/");
            return null;
        });

        post("/sortAjax", (request, response) -> {
            response.type("application/json");
            String sort = request.queryParams("sortAjax");
            ArrayList<Product> products;
            Gson gson = new Gson();
            if (dataMap.containsKey("productSearch")) {
                products = toSortProductSearch[0];
            } else {
                products = ProductJsonActions.readFromJson(productJson);
            }
            ArrayList<Product> sortedProducts = new ArrayList<>();
            switch (sort) {
                case "nameAsc":
                    products.sort((product1, product2) -> product1.getName().compareToIgnoreCase(product2.getName()));
                    sortedProducts = products;
                    break;
                case "nameDesc":
                    products.sort((product1, product2) -> product2.getName().compareToIgnoreCase(product1.getName()));
                    sortedProducts = products;
                    break;
                case "priceAsc":
                    products.sort(Comparator.comparingDouble(Product::getPrice));
                    sortedProducts = products;
                    break;
                case "priceDesc":
                    products.sort(Collections.reverseOrder(Comparator.comparingDouble(Product::getPrice)));
                    sortedProducts = products;
                    break;
                case "default":
                    sortedProducts = products;
                    break;
            }
            dataMap.remove("sortBy");
            dataMap.remove("sort");
            dataMap.remove("clear");
            return gson.toJson(sortedProducts);
        });

        post("/payment", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            response.type("application/json");
            int id = Integer.parseInt(request.queryParams("userID"));
            Gson gson = new Gson();
            ArrayList<Order> orders = OrderJsonActions.readFromJson(orderJson);
            ArrayList<Order> myOrders = new ArrayList<>();
            for (Order order : orders) {
                if (order.getUser().getUid() == id) {
                    myOrders.add(order);
                }
            }
            return gson.toJson(myOrders);
        });

        post("/restore", (request, response) -> {
            dataMap.remove("productSearch");
            dataMap.remove("sortedProducts");
            dataMap.remove("messageSearchFound");
            dataMap.remove("messageSearchEmpty");
            dataMap.remove("messageSearchNotFound");
            dataMap.remove("sortBy");
            dataMap.remove("sort");
            dataMap.remove("term");
            dataMap.put("clear", "clear");
            response.redirect("/");
            return null;
        });

        get("/loginForm", (request, response) -> new HandlebarsTemplateEngine().render(
                new ModelAndView(null, "loginForm.hbs")));

        post("/login", (request, response) -> {
            String username = request.queryParams("userName");
            String password = request.queryParams("passWord");
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    if (user.getStatus().equals("admin")) {
                        request.session().attribute("userStatus", "admin");
                        dataMap.put("userStatus", "admin");
                        dataMap.put("welcomeUser", username);
                        response.redirect("/adminPanel");
                    } else {
                        request.session().attribute("userStatus", "user");
                        dataMap.put("userStatus", "user");
                        dataMap.put("welcomeUser", username);
                        response.redirect("/userPanel/" + user.getUid());
                    }
                    return null;
                }
            }
            String msg = "Error: wrong username and password";
            dataMap.put("messageUserLogin", msg);
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "loginForm.hbs"));
        });

        get("/logout", (request, response) -> {
            if (request.session().attribute("userStatus").toString().equals("user")) {
                ArrayList<Product> cart = new ArrayList<>();
                ProductJsonActions.writeToJSON(cart, cartJson);
            }
            request.session().removeAttribute("userStatus");
            dataMap.remove("userStatus");
            dataMap.remove("userAdmin");
            dataMap.remove("userUser");
            dataMap.remove("currentUserIn");
            dataMap.remove("addToCart");
            dataMap.remove("removeFromCart");
            dataMap.remove("orderPaid");
            response.redirect("/");
            return null;
        });

        get("/registerNewUser", (request, response) -> new HandlebarsTemplateEngine().render(
                new ModelAndView(dataMap, "registerNewUserForm.hbs")));

        post("/registerNewUser/add", (request, response) -> {
            String username = request.queryParams("userName");
            username = username.trim();
            String password = request.queryParams("passWord");
            password = password.trim();
            String status = request.queryParams("status");
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            int index = users.stream().mapToInt(User::getUid).filter(user -> user >= 0).max().orElse(0);
//            int index = 0;
//            for (User user : users) {
//                if (user.getUid() > index) {
//                    index = user.getUid();
//                }
//            }
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    String msg = "Username {" + username + "} already exists.";
                    dataMap.put("messageNewUserState", msg);
                    response.redirect("/registerNewUser");
                    return null;
                }
            }
            User.setID(index);
            String msg = "";
            if (!username.isEmpty() && !password.isEmpty()) {
                users.add(new User(username, password, status));
                msg = "Registered new user: " + username + " - {" + status + "}.";
            } else {
                msg = "You must set username and password.";
            }
            dataMap.put("messageNewUserState", msg);
            if (UserJsonActions.writeToJSON(users, userJson)) {
                response.redirect("/");
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "registerNewUserForm.hbs"));
            }
        });

        // User panel ***************************************************************************************

        get("/userPanel/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            ArrayList<Product> cart = ProductJsonActions.readFromJson(cartJson);
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            int id = Integer.parseInt(request.params(":id"));
            for (User tmpUser : users) {
                if (tmpUser.getUid() == id) {
                    dataMap.put("currentUserIn", tmpUser);
                }
            }
            dataMap.put("cartSize", cart.size());
            dataMap.put("currentUser", UserJsonActions.readFromJson(userJson).stream().filter(user -> user.getUid() == id).toArray());
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "userPanel.hbs"));
        });

        get("/userPanel/edit/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            dataMap.put("userPanel", UserJsonActions.readFromJson(userJson).stream().filter(user -> user.getUid() == id).toArray());
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "userPanelEditUser.hbs"));
        });

        post("/userPanel/update/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            String username = request.queryParams("userName");
            username = username.trim();
            String password = request.queryParams("passWord");
            password = password.trim();
            String status = request.queryParams("status");
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    String msg = "Username {" + username + "} already exists.";
                    dataMap.put("messageUserPanelState", msg);
                    response.redirect("/userPanel/edit/" + id);
                    return null;
                }
            }
            String msg = "";
            if (!username.isEmpty() && !password.isEmpty()) {
                for (User user : users) {
                    if (user.getUid() == id) {
                        msg = "User: {" + user;
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setStatus(status);
                        msg += "}, updated to: {" + user + "}.";
                    }
                }
            } else {
                msg = "You must set username and password.";
            }
            dataMap.put("messageUserPanelState", msg);
            if (UserJsonActions.writeToJSON(users, userJson)) {
                response.redirect("/userPanel/" + id);
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "userPanel.hbs"));
            }
        });

        // Order routes ************************************************************************************

        get("/userPanel/orders/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<Order> orders = OrderJsonActions.readFromJson(orderJson);
            ArrayList<Order> myOrders = new ArrayList<>();
            for (Order order : orders) {
                if (order.getUser().getUid() == id) {
                    myOrders.add(order);
                }
            }
            dataMap.put("myOrders", myOrders);
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "myOrder.hbs"));
        });

        post("/orders/add/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<Product> cart = ProductJsonActions.readFromJson(cartJson);
            ArrayList<Order> orders = OrderJsonActions.readFromJson((orderJson));
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            User currentUser = null;
            for (User user : users) {
                if (user.getUid() == id) {
                    currentUser = user;
                }
            }
            int index = orders.stream().mapToInt(Order::getOid).filter(order -> order >= 0).max().orElse(0);
            Order.setID(index);
            if (currentUser != null && cart != null) {
                orders.add(new Order(currentUser, cart));
                cart = new ArrayList<>();
                ProductJsonActions.writeToJSON(cart, cartJson);
                dataMap.put("cartSize", cart.size());
            } else {
                dataMap.put("userNotFound", "Unable to buy product with ID=" + id + ", user is not set.");
            }
            if (OrderJsonActions.writeToJSON(orders, orderJson)) {
                response.redirect("/userPanel/orders/" + id);
                return null;
            } else {
                String msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "index.hbs"));
            }
        });

        post("/orders/pay/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<Order> orders = OrderJsonActions.readFromJson((orderJson));
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            User currentUser = null;
            if (dataMap.containsKey("currentUserIn")) {
                String tmpString = dataMap.get("currentUserIn").toString();
                String[] tmpStringToArray = tmpString.split(" ");
                for (User user : users) {
                    if (tmpStringToArray[1].equals(user.getUsername())) {
                        currentUser = user;
                    }
                }
            }
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getOid() == id) {
                    orders.get(i).setPaid(true);
                    dataMap.put("orderPaid", "Order: [" + orders.get(i).getOid() + "] is paid.");
                    dataMap.remove("addToCart");
                    dataMap.remove("removeFromCart");
                }
            }
            if (OrderJsonActions.writeToJSON(orders, orderJson)) {
                response.redirect("/userPanel/orders/" + currentUser.getUid());
                return null;
            } else {
                String msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "index.hbs"));
            }
        });

        // Cart routes *********************************************************************************

        get("/userPanel/cart/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            //int id = Integer.parseInt(request.params(":id"));
            ArrayList<Product> cart = ProductJsonActions.readFromJson(cartJson);
            if (cart.isEmpty()) {
                dataMap.put("emptyCart", "The cart is empty, you haven't bought any products yet.");
            }
            double costPreview = 0;
            for (Product product : cart) {
                costPreview += product.getPrice();
            }
            dataMap.put("costPreview", costPreview);
            dataMap.put("myCart", cart);
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "myCart.hbs"));
        });

        post("/cart/add/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            ArrayList<Product> cart = ProductJsonActions.readFromJson(cartJson);
            User currentUser = null;
            if (dataMap.containsKey("currentUserIn")) {
                String tmpString = dataMap.get("currentUserIn").toString();
                String[] tmpStringToArray = tmpString.split(" ");
                for (User user : users) {
                    if (tmpStringToArray[1].equals(user.getUsername())) {
                        currentUser = user;
                    }
                }
            }
            if (currentUser != null) {
                for (Product product : products) {
                    if (product.getPid() == id) {
                        cart.add(product);
                        dataMap.put("addToCart", "Product: [" + product.getName() + "], added to cart.");
                        dataMap.remove("removeFromCart");
                        dataMap.remove("emptyCart");
                    }
                }
                dataMap.put("cartSize", cart.size());
            } else {
                dataMap.put("userNotFound", "Unable to buy product with ID=" + id + ", user is not set.");
            }
            if (ProductJsonActions.writeToJSON(cart, cartJson)) {
                response.redirect("/");
                return null;
            } else {
                String msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "index.hbs"));
            }
        });

        post("/cart/remove/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("admin")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            ArrayList<Product> cart = ProductJsonActions.readFromJson(cartJson);
            User currentUser = null;
            if (dataMap.containsKey("currentUserIn")) {
                String tmpString = dataMap.get("currentUserIn").toString();
                String[] tmpStringToArray = tmpString.split(" ");
                for (User user : users) {
                    if (tmpStringToArray[1].equals(user.getUsername())) {
                        currentUser = user;
                    }
                }
            }
            if (currentUser != null) {
                boolean duplicates = false;
                int count = 0;
                int tmpID = 0;
                for (int i = 0; i < cart.size(); i++) {
                    if (cart.get(i).getPid() == id) {
                        ++count;
                        tmpID = i;
                    }
                }
                if (count > 1) {
                    duplicates = true;
                }
                if (duplicates) {
                    dataMap.put("removeFromCart", "Product: [" + cart.get(tmpID).getName() + "], removed from cart.");
                    cart.remove(tmpID);
                    dataMap.remove("addToCart");
                } else {
                    for (int i = 0; i < cart.size(); i++) {
                        if (cart.get(i).getPid() == id) {
                            dataMap.put("removeFromCart", "Product: [" + cart.get(i).getName() + "], removed from cart.");
                            cart.remove(i);
                            dataMap.remove("addToCart");
                        }
                    }
                }
                dataMap.put("cartSize", cart.size());
            } else {
                dataMap.put("userNotFound", "Unable to buy product with ID=" + id + ", user is not set.");
            }
            if (ProductJsonActions.writeToJSON(cart, cartJson)) {
                response.redirect("/userPanel/cart/" + currentUser.getUid());
                return null;
            } else {
                String msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "index.hbs"));
            }
        });

        // Admin panel ****************************************************************************************

        get("/adminPanel", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            ArrayList<Category> categories = CategoryJsonActions.readFromJson(categoryJson);
            ArrayList<Order> orders = OrderJsonActions.readFromJson((orderJson));
            int countAdmin = 0;
            for (User user : users) {
                if (user.getStatus().equals("admin")) {
                    countAdmin++;
                }
            }
            int countPaid = 0;
            for (Order order : orders) {
                if (order.isPaid()) {
                    countPaid++;
                }
            }
            int countUser = users.size() - countAdmin;
            int countNotPaid = orders.size() - countPaid;
            dataMap.put("totalUsers", "Total users: " + users.size() + " [admins: " + countAdmin + ", users: " + countUser + "]");
            dataMap.put("totalProducts", "Total products: " + products.size());
            dataMap.put("totalCategories", "Total categories: " + categories.size());
            dataMap.put("totalOrders", "Total orders: " + orders.size() + " [paid: " + countPaid + ", not paid: " + countNotPaid + "]");
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "adminPanel.hbs"));
        });

        // Admin panel orders ***********************************************************************

        get("/adminPanel/orders", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            ArrayList<Order> orders = OrderJsonActions.readFromJson((orderJson));
            int countPaid = 0;
            for (Order order : orders) {
                if (order.isPaid()) {
                    countPaid++;
                }
            }
            int countNotPaid = orders.size() - countPaid;
            dataMap.put("totalOrders", "Total orders: " + orders.size() + " [paid: " + countPaid + ", not paid: " + countNotPaid + "]");
            dataMap.put("orders", orders);
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "orders.hbs"));
        });

        // User routes ****************************************************************************************

        get("/adminPanel/users", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            int countAdmin = 0;
            for (User user : users) {
                if (user.getStatus().equals("admin")) {
                    countAdmin++;
                }
            }
            int countUser = users.size() - countAdmin;
            dataMap.put("totalUsers", "Total users: " + users.size() + " [admins: " + countAdmin + ", users: " + countUser + "]");
            dataMap.put("users", UserJsonActions.readFromJson(userJson));
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "users.hbs"));
        });

        get("/adminPanel/users/new", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "usersForm.hbs"));
        });

        get("/adminPanel/users/edit/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            dataMap.put("user", UserJsonActions.readFromJson(userJson).stream().filter(user -> user.getUid() == id).toArray());
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "usersEdit.hbs"));
        });

        post("/adminPanel/users/update/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            String username = request.queryParams("userName");
            username = username.trim();
            String password = request.queryParams("passWord");
            password = password.trim();
            String status = request.queryParams("status");
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    String msg = "Username {" + username + "} already exists.";
                    dataMap.put("messageUserState", msg);
                    response.redirect("/adminPanel/users/edit/" + id);
                    return null;
                }
            }
            String msg = "";
            if (!username.isEmpty() && !password.isEmpty()) {
                for (User user : users) {
                    if (user.getUid() == id) {
                        msg = "User: {" + user;
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setStatus(status);
                        msg += "}, updated to: {" + user + "}.";
                    }
                }
            } else {
                msg = "You must set all fields.";
            }
            dataMap.put("messageUserState", msg);
            if (UserJsonActions.writeToJSON(users, userJson)) {
                response.redirect("/adminPanel/users");
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "users.hbs"));
            }
        });

        post("/adminPanel/users/add", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            String username = request.queryParams("userName");
            username = username.trim();
            String password = request.queryParams("passWord");
            password = password.trim();
            String status = request.queryParams("status");
            status = status.trim();
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            int index = users.stream().mapToInt(User::getUid).filter(user -> user >= 0).max().orElse(0);
//            int index = 0;
//            for (User user : users) {
//                if (user.getUid() > index) {
//                    index = user.getUid();
//                }
//            }
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    String msg = "Username {" + username + "} already exists.";
                    dataMap.put("messageUserState", msg);
                    response.redirect("/adminPanel/users/new");
                    return null;
                }
            }
            User.setID(index);
            String msg = "";
            if (!username.isEmpty() && !password.isEmpty() && !status.isEmpty()) {
                users.add(new User(username, password, status));
                msg = "Added new user: " + username + " - {" + status + "}.";
            } else {
                msg = "You must set all fields.";
            }
            dataMap.put("messageUserState", msg);
            if (UserJsonActions.writeToJSON(users, userJson)) {
                response.redirect("/adminPanel/users");
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "usersForm.hbs"));
            }
        });

        post("/adminPanel/users/delete/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<User> users = UserJsonActions.readFromJson(userJson);
            for (User user : users) {
                if (user.getUid() == id) {
                    if (user.getStatus().equals(request.session().attribute("userStatus").toString())) {
                        String msg = "Admin user {" + user.getUsername() + "} can't be deleted.";
                        dataMap.put("messageUserState", msg);
                        response.redirect("/adminPanel/users");
                        return null;
                    }
                    String msg = "Success: user {" + user.getUsername() + "} was deleted.";
                    dataMap.put("messageUserState", msg);
                }
            }
            users.removeIf(user -> user.getUid() == id);
            if (UserJsonActions.writeToJSON(users, userJson)) {
                response.redirect("/adminPanel/users");
                return null;
            } else {
                String msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "users.hbs"));
            }
        });

        // Product routes ************************************************************************************

        get("/adminPanel/products", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            dataMap.put("totalProducts", "Total products: " + products.size());
            dataMap.put("products", ProductJsonActions.readFromJson(productJson));
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "products.hbs"));
        });

        get("/adminPanel/products/new", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            dataMap.put("categories", CategoryJsonActions.readFromJson(categoryJson));
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "productsForm.hbs"));
        });

        get("/adminPanel/products/edit/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            dataMap.put("categories", CategoryJsonActions.readFromJson(categoryJson));
            dataMap.put("product", ProductJsonActions.readFromJson(productJson).stream().filter(product -> product.getPid() == id).toArray());
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "productsEdit.hbs"));
        });

        post("/adminPanel/products/update/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            String name = request.queryParams("productName");
            name = name.trim();
            String priceString = request.queryParams("productPrice");
            priceString = priceString.trim();
            String info = request.queryParams("productInfo");
            info = info.trim();
            int ind = Integer.parseInt(request.queryParams("categorySelect"));
            ArrayList<Category> categories = CategoryJsonActions.readFromJson(categoryJson);
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            for (Product product : products) {
                if (product.getName().equals(name) && product.getPid() == id) {
                    break;
                } else if (product.getName().equals(name)) {
                    String msg = "Product {" + name + "} already exists.";
                    dataMap.put("messageProductState", msg);
                    response.redirect("/adminPanel/products/edit/" + id);
                    return null;
                }
            }
            String msg = "";
            if (!name.isEmpty() && !priceString.isEmpty() && !info.isEmpty()) {
                double price = Double.parseDouble(priceString);
                for (Product product : products) {
                    if (product.getPid() == id) {
                        msg = "Product: {[" + product.getPid() + ": " + product.getName() + ", " + product.getPrice() + ", " + product.getInfo() + ", (" + product.getCategory().getName() + ")]";
                        product.setName(name);
                        product.setPrice(price);
                        product.setInfo(info);
                        for (Category category : categories) {
                            if (category.getCid() == ind) {
                                product.setCategory(category);
                                msg += "}, updated to: {[" + product.getPid() + ": " + product.getName() + ", " + product.getPrice() + ", " + product.getInfo() + ", (" + product.getCategory().getName() + ")]}.";

                            }
                        }
                    }
                }
            } else {
                msg = "You must set all fields.";
            }
            dataMap.put("messageProductState", msg);
            if (ProductJsonActions.writeToJSON(products, productJson)) {
                response.redirect("/adminPanel/products");
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "products.hbs"));
            }
        });

        post("/adminPanel/products/add", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            String name = request.queryParams("productName");
            name = name.trim();
            String priceString = request.queryParams("productPrice");
            priceString = priceString.trim();
            String info = request.queryParams("productInfo");
            info = info.trim();
            int id = Integer.parseInt(request.queryParams("categorySelect"));
            ArrayList<Category> categories = CategoryJsonActions.readFromJson(categoryJson);
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            int index = products.stream().mapToInt(Product::getPid).filter(product -> product >= 0).max().orElse(0);
//            int index = 0;
//            for (Product product : products) {
//                if (product.getPid() > index) {
//                    index = product.getPid();
//                }
//            }
            for (Product product : products) {
                if (product.getName().equals(name)) {
                    String msg = "Product {" + name + "} already exists.";
                    dataMap.put("messageProductState", msg);
                    response.redirect("/adminPanel/products/new");
                    return null;
                }
            }
            Product.setID(index);
            String msg = "";
            if (!name.isEmpty() && !priceString.isEmpty() && !info.isEmpty() && id != 0) {
                double price = Double.parseDouble(priceString);
                for (Category category : categories) {
                    if (category.getCid() == id) {
                        products.add(new Product(name, price, info, category));
                        msg = "Added new product: " + name + ".";
                    }
                }
            } else {
                msg = "You must set all fields.";
            }
            dataMap.put("messageProductState", msg);
            if (ProductJsonActions.writeToJSON(products, productJson)) {
                response.redirect("/adminPanel/products");
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "productsForm.hbs"));
            }
        });

        post("/adminPanel/products/delete/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            for (Product product : products) {
                if (product.getPid() == id) {
                    String msg = "Success: product {" + product.getName() + "} was deleted.";
                    dataMap.put("messageProductState", msg);
                }
            }
            products.removeIf(product -> product.getPid() == id);
            if (ProductJsonActions.writeToJSON(products, productJson)) {
                response.redirect("/adminPanel/products");
                return null;
            } else {
                String msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "products.hbs"));
            }
        });

        // Category routes **************************************************************************************

        get("/adminPanel/categories", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            ArrayList<Category> categories = CategoryJsonActions.readFromJson(categoryJson);
            dataMap.put("totalCategories", "Total categories: " + categories.size());
            dataMap.put("categories", CategoryJsonActions.readFromJson(categoryJson));
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "categories.hbs"));
        });

        get("/adminPanel/categories/new", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "categoriesForm.hbs"));
        });

        get("/adminPanel/categories/edit/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            dataMap.put("category", CategoryJsonActions.readFromJson(categoryJson).stream().filter(category -> category.getCid() == id).toArray());
            return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "categoriesEdit.hbs"));
        });

        post("/adminPanel/categories/update/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            String name = request.queryParams("categoryName");
            name = name.trim();
            ArrayList<Category> categories = CategoryJsonActions.readFromJson(categoryJson);
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            for (Category category : categories) {
                if (category.getName().equals(name)) {
                    String msg = "Category {" + name + "} already exists.";
                    dataMap.put("messageCategoryState", msg);
                    response.redirect("/adminPanel/categories/edit/" + id);
                    return null;
                }
            }
            String msg = "";
            if (!name.isEmpty()) {
                for (Category category : categories) {
                    if (category.getCid() == id) {
                        msg = "Category: {" + category.getName() + "}, updated to: {" + name + "}.";
                        category.setName(name);
                        dataMap.put("messageCategoryState", msg);
                        for (Product product : products) {
                            if (product.getCategory().getCid() == id) {
                                product.setCategory(category);
                            }
                        }
                        ProductJsonActions.writeToJSON(products, productJson);
                    }
                }
            } else {
                msg = "You didn't set category name.";
            }
            dataMap.put("messageCategoryState", msg);
            if (CategoryJsonActions.writeToJSON(categories, categoryJson)) {
                response.redirect("/adminPanel/categories");
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "categories.hbs"));
            }
        });

        post("/adminPanel/categories/add", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            String name = request.queryParams("categoryName");
            name = name.trim();
            ArrayList<Category> categories = CategoryJsonActions.readFromJson(categoryJson);
            int index = categories.stream().mapToInt(Category::getCid).filter(category -> category >= 0).max().orElse(0);
//            int index = 0;
//            for (Category category : categories) {
//                if (category.getCid() > index) {
//                    index = category.getCid();
//                }
//            }
            for (Category category : categories) {
                if (category.getName().equals(name)) {
                    String msg = "Category {" + name + "} already exists.";
                    dataMap.put("messageCategoryState", msg);
                    response.redirect("/adminPanel/categories/new");
                    return null;
                }
            }
            Category.setID(index);
            String msg = "";
            if (!name.isEmpty()) {
                categories.add(new Category(name));
                msg = "Added new category: " + name + ".";
            } else {
                msg = "You didn't set category name.";
            }
            dataMap.put("messageCategoryState", msg);
            if (CategoryJsonActions.writeToJSON(categories, categoryJson)) {
                response.redirect("/adminPanel/categories");
                return null;
            } else {
                msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "categoriesForm.hbs"));
            }
        });

        post("/adminPanel/categories/delete/:id", (request, response) -> {
            if (!dataMap.containsKey("userStatus")) {
                response.redirect("/");
                return null;
            }
            if (request.session().attribute("userStatus").toString().equals("user")) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params(":id"));
            ArrayList<Category> categories = CategoryJsonActions.readFromJson(categoryJson);
            ArrayList<Product> products = ProductJsonActions.readFromJson(productJson);
            Iterator<Category> categoryIterator = categories.iterator();
            while (categoryIterator.hasNext()) {
                Category category = categoryIterator.next();
                if (category.getCid() == id) {
                    int count = 0;
                    for (Product product : products) {
                        if (product.getCategory().getCid() == id) {
                            count++;
                        }
                    }
                    String msg;
                    if (count == 0) {
                        msg = "Success: category {" + category.getName() + "} was deleted.";
                        categoryIterator.remove();
                    } else {
                        msg = "Error: category {" + category.getName() + "} not empty, can't be deleted.";
                    }
                    dataMap.put("messageCategoryState", msg);
                }
            }
            if (CategoryJsonActions.writeToJSON(categories, categoryJson)) {
                response.redirect("/adminPanel/categories");
                return null;
            } else {
                String msg = "Error: failed to write to json, returned to previous state.";
                dataMap.put("messageJson", msg);
                return new HandlebarsTemplateEngine().render(new ModelAndView(dataMap, "categories.hbs"));
            }
        });
    }
}
