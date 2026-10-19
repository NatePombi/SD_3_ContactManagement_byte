export default function Header({title,subtitle}) {
    return (
        <header className="page-header">
            <div>
                <span className="eyebrow">
                    CONTACT MANAGEMENT SYSTEM 
                </span>

                <h1>{title}</h1>

                <p>{subtitle}</p>
            </div>

            
        </header>
    );
}