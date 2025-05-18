using Microsoft.AspNetCore.Mvc;


namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class UserController : Controller
    {
        // GET: /Admin/User
        public IActionResult Index()
        {
            // TODO: gọi service lấy danh sách users
            // var users = _userService.GetAll();
            // return View(users);

            return View(); // tạm thời
        }
    }
}
