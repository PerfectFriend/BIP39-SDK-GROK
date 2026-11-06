import SwiftUI

/// Welcome Aboard — iOS counterpart to the Android splash (Cyberpunk × Van Gogh × Gaudí tokens).
public struct WelcomeAboardView: View {
    public var onCreate: () -> Void
    public var onRestore: () -> Void
    public var accent: Color
    public var magenta: Color
    public var voidColor: Color

    public init(
        accent: Color = Color(red: 0, green: 0.94, blue: 1),
        magenta: Color = Color(red: 1, green: 0.18, blue: 0.58),
        voidColor: Color = Color(red: 0.04, green: 0.02, blue: 0.09),
        onCreate: @escaping () -> Void,
        onRestore: @escaping () -> Void
    ) {
        self.accent = accent
        self.magenta = magenta
        self.voidColor = voidColor
        self.onCreate = onCreate
        self.onRestore = onRestore
    }

    public var body: some View {
        ZStack {
            voidColor.ignoresSafeArea()
            VStack(spacing: 24) {
                Spacer()
                Text("Welcome Aboard")
                    .font(.largeTitle.bold())
                    .foregroundStyle(Color(red: 0.95, green: 0.94, blue: 0.91))
                Text("Your keys. Your rules.")
                    .font(.subheadline)
                    .foregroundStyle(Color(red: 0.60, green: 0.58, blue: 0.72))
                Spacer()
                Button(action: onCreate) {
                    Text("Create account")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(accent)
                        .foregroundStyle(voidColor)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                Button(action: onRestore) {
                    Text("Restore account")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(magenta.opacity(0.85), lineWidth: 1.5)
                        )
                        .foregroundStyle(Color(red: 0.95, green: 0.94, blue: 0.91))
                }
                .padding(.bottom, 40)
            }
            .padding(.horizontal, 28)
        }
    }
}
