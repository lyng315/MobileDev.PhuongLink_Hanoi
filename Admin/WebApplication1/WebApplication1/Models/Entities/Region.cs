namespace WebApplication1.Models.Entities
{
    public class Region
    {
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public int? ParentRegionId { get; set; }
    }
}
